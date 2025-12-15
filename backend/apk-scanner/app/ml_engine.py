import joblib
import pandas as pd
import numpy as np
import os
import logging
import xgboost as xgb

# Initialize Logger
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# --- SINGLETON MODEL LOADING ---
_model = None
_features = None

def load_model():
    """
    Loads the XGBoost model and feature list globally.
    Singleton Pattern: Loads only once.
    """
    global _model, _features
    if _model is not None and _features is not None:
        return _model, _features

    try:
        base_dir = os.path.dirname(os.path.abspath(__file__))
        scanner_dir = os.path.dirname(base_dir) # .../backend/apk-scanner
        
        # Path to the new XGBoost model
        # Try both locations to be safe/robust
        paths_to_try = [
             os.path.join(scanner_dir, "models", "malware_model_xgb.pkl"),
             os.path.abspath("models/malware_model_xgb.pkl")
        ]
        
        model_path = None
        for p in paths_to_try:
            if os.path.exists(p):
                model_path = p
                break
                
        features_path = os.path.join(scanner_dir, "models", "model_features.pkl")
        if not os.path.exists(features_path):
             features_path = os.path.abspath("models/model_features.pkl")

        if not model_path or not os.path.exists(model_path):
            logger.error(f"XGBoost Model missing. Searched: {paths_to_try}")
            return None, None
            
        if not os.path.exists(features_path):
            logger.error(f"Model Features missing at {features_path}")
            return None, None

        logger.info(f"Loading XGBoost Model from: {model_path}")
        _model = joblib.load(model_path)
        _features = joblib.load(features_path)
        logger.info("XGBoost Model Loaded Successfully.")
        
        return _model, _features

    except Exception as e:
        logger.error(f"Failed to load ML Model: {str(e)}")
        return None, None

def predict_score(permissions_list):
    """
    Predicts malware probability using XGBoost.
    
    Args:
        permissions_list (list): List of permission strings.
        
    Returns:
        dict: {'probability': float, 'label': str, 'color': str}
    """
    model, features = load_model()
    
    # Fail-Safe: Return None if model unavailable (Static analysis continues)
    if model is None or features is None:
        logger.warning("ML Model not available. Skipping prediction.")
        return None
        
    try:
        # --- VECTORIZATION LOGIC (The "Translation" Layer) ---
        # 1. Create Zero Vector
        input_vector = np.zeros(len(features))
        
        # 2. Pre-process input permissions (Handle naming mismatches)
        # Convert "android.permission.SEND_SMS" -> "SEND_SMS" for robust matching
        # Also keep original just in case model uses full names
        clean_input_perms = set()
        for p in permissions_list:
            clean_input_perms.add(p)
            clean_input_perms.add(p.split('.')[-1]) # Short name
            
        # 3. Fill Vector based on Feature Map Order
        for idx, feature_name in enumerate(features):
            # Check if feature exists in input (Full or Short name)
            # Feature in model might be "SEND_SMS" or "android.permission.SEND_SMS"
            # We match against our clean set
            
            # Direct match or Short name match
            if feature_name in clean_input_perms:
                 input_vector[idx] = 1
            elif feature_name.split('.')[-1] in clean_input_perms:
                 input_vector[idx] = 1

        # 4. Predict
        # XGBoost via Sklearn interface (joblib) usually accepts 2D array
        # Reshape to (1, n_features)
        input_vector = input_vector.reshape(1, -1)
        
        # Get Probability of Class 1 (Malware)
        prob = model.predict_proba(input_vector)[0][1]
        
        # Convert to percentage
        probability = float(round(prob * 100, 2))
        
        # --- LABELING ---
        if probability < 30:
            label = "Safe"
            color = "#27ae60" # Green
        elif probability < 70:
            label = "Suspicious"
            color = "#f39c12" # Orange
        else:
            label = "Critical"
            color = "#c0392b" # Red
            
        return {
            "probability": probability,
            "label": label,
            "color": color,
            "details": f"XGBoost Confidence: {probability}%"
        }

    except Exception as e:
        logger.error(f"XGBoost Prediction Failed: {e}")
        return None # Fail-Safely
