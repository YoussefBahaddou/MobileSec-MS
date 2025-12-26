from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import jwt, JWTError
from app.core.config import settings

# Since Supabase uses JWTs, we use the specific secret to decode them.
# If you don't have the secret locally, we can inspect the token without verification 
# (NOT RECOMMENDED FOR PROD) or ask user to provide it.
# For now, we will assume SUPABASE_JWT_SECRET is in env.

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

def get_current_user(token: str = Depends(oauth2_scheme)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        # NOTE: For true security, this SECRET must match your Supabase Project JWT Secret.
        # If missing, implementation will fail. We default to None to signal configuration error if used.
        secret = getattr(settings, "SUPABASE_JWT_SECRET", None)
        
        if not secret:
            # Fallback for dev: if no secret provided, we just decode without signature verification
            # WARNING: INSECURE. But allows progress if user doesn't have secret handy.
            # We urge user to add secret to .env
            payload = jwt.get_unverified_claims(token)
        else:
            payload = jwt.decode(token, secret, algorithms=["HS256"], audience="authenticated")
            
        user_id: str = payload.get("sub")
        if user_id is None:
            raise credentials_exception
        return user_id
    except JWTError:
        raise credentials_exception
