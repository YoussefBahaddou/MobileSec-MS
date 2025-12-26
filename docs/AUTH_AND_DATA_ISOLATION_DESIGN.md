# Authentication & Data Isolation Design

## 1. UI Flow: The User Journey

The user experience is designed to be secure yet seamless, ensuring users always know their authentication state.

### Step 1: Registration (New Users)
1.  **Entry Point**: User accesses `/register`.
2.  **Action**: User enters Email and Password (and optionally confirms password).
3.  **System**: Frontend calls Supabase `signUp`.
4.  **Feedback**:
    *   *Success*: System shows "Registration Successful! Please check your email to verify" (if email verification is on) or auto-logs them in.
    *   *Error*: Shows "Email already in use" or "Password too weak".
5.  **Transition**: Redirects to `/login`.

### Step 2: Login (Returning Users)
1.  **Entry Point**: User accesses `/login` (or is redirected here when trying to access `/dashboard`).
2.  **Action**: User enters Credentials.
3.  **System**: Frontend calls Supabase `signIn`.
    *   **Token Storage**: Supabase client automatically stores the Session (JWT) in `localStorage`.
4.  **Transition**: Redirects to `/dashboard`.

### Step 3: Personalized Dashboard
1.  **View**: User sees the Dashboard.
2.  **Data Fetching**: Frontend makes a call to `GET /api/dashboard/stats`.
    *   **Crucial**: The Authorization Header (`Bearer <token>`) is automatically attached.
    *   **Result**: The user *only* sees stats (Risk Distribution, Recent Scans) for APKs *they* uploaded.

### Step 4: Uploading & Scanning
1.  **Action**: User uploads an APK in `/upload`.
2.  **System**: The Backend extracts the `user_id` from the Auth Token.
3.  **Storage**: The Scan Result is saved in the database tagged with this `user_id`.

---

## 2. Dashboard Logic: "My Data Only"

To ensure users never see each other's data, the Logic changes from "Get All" to "Get Mine".

### Concept
Currently, the system queries: `SELECT * FROM scans`
New Logic will query: `SELECT * FROM scans WHERE user_id = current_user_id`

### Implementation
1.  **Frontend**: Does not need to send `user_id` explicitly. It sends the **Access Token**.
2.  **Backend (APK Scanner)**:
    *   Middleware intercepts the request.
    *   Validates the Token.
    *   Extracts `user_id` (Subject) from the Token.
    *   Passes `user_id` to the Service Layer.
    *   Service Layer adds `.filter(APKMetadata.user_id == user_id)` to all database queries.

---

## 3. Data Isolation & Security Strategy

### Database Schema Changes
We must update the `APKMetadata` model (in `apk-scanner` service) to include ownership.

```python
class APKMetadata(Base):
    # ... existing fields ...
    user_id = Column(String, index=True) # UUID from Supabase Auth
```

### Flow of Data
1.  **Upload**:
    *   `POST /api/scan/analyze`
    *   Header: `Authorization: Bearer <JWT>`
    *   Backend decodes JWT -> gets `user_123`.
    *   Backend saves Scan -> `INSERT INTO apk_metadata ... user_id='user_123'`.

2.  **Reading (Dashboard)**:
    *   `GET /api/dashboard/stats`
    *   Backend decodes JWT -> gets `user_123`.
    *   Backend Query: `SELECT count(*) FROM apk_metadata WHERE user_id='user_123'`.

### Security Layer (Row Level Security - RLS)
*   **If using Postgres (Supabase DB)** directly: We enable RLS policies.
    *   `CREATE POLICY "Users can see own scans" ON "apk_metadata" FOR SELECT USING (auth.uid() = user_id);`
*   **If using SQLite (Current APK Scanner)**: We rely on **Application-Level Security**. The Python code *must* strictly enforce the filter.

---

## 4. Implementation Strategy

### A. Frontend (React)
1.  **`AuthContext`**: A global state provider that holds the `session` object.
2.  **`Axios Interceptor`**: Automatically injects the token into every request.
    ```javascript
    axios.interceptors.request.use(config => {
        const token = supabase.auth.session()?.access_token;
        if (token) config.headers.Authorization = `Bearer ${token}`;
        return config;
    });
    ```
3.  **Routing**:
    ```javascript
    <Route path="/dashboard" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
    ```

### B. Backend (Python/FastAPI)
1.  **Dependency Injection**: Create a `get_current_user` dependency.
    ```python
    def get_current_user(token: str = Depends(oauth2_scheme)):
        # Verify token with Supabase Secret
        # Return user_id
    ```
2.  **Updated Endpoints**:
    ```python
    @router.get("/stats")
    def get_dashboard_stats(db: Session, user_id: str = Depends(get_current_user)):
        # Query only for this user
        scans = db.query(APKMetadata).filter(APKMetadata.user_id == user_id).all()
    ```

---

## 5. Best Practices & Pitfalls

### ✅ Best Practices
*   **Token-Based Auth**: Never store state in the server (Stateless). Use JWTs.
*   **Least Privilege**: The frontend should only ask for data it needs. The backend should only return data the user owns.
*   **Secure Storage**: Store tokens in memory or HttpOnly cookies (Supabase client handles `localStorage` by default which is acceptable for this type of app, but HttpOnly cookies are strictly safer).
*   **Type Safety**: Ensure the `user_id` is always a UUID and validated.

### ❌ Pitfalls to Avoid
*   **"Client-Side Filtering"**: NEVER fetch all data to the frontend and then `filter()` it in JavaScript. A hacker can just inspect the Network tab and see everyone's data. **Filtering must happen on the Backend/Database.**
*   **Trusting User Input**: Never let the user send `{ "user_id": "other_user" }` in the body. Always derive ID from the trusted Token.
*   **Leaking Metadata**: Ensure global stats (e.g., "Total Malicious Apps found by System") are aggregated anonymized, while personal stats are isolated.
