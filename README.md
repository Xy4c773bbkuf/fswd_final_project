# SkillSwap

SkillSwap is a peer-to-peer learning platform where students exchange knowledge using a transactional credit ledger.

## Run locally

1. Create a MySQL database named `skillswap` and set `DB_USERNAME` and `DB_PASSWORD` if needed. Start the backend from `backend/` with Maven:

   ```powershell
   mvn spring-boot:run
   ```

2. In a second terminal, install and start the frontend:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

The web app is available at `http://localhost:5173`, the API at `http://localhost:8080`, and OpenAPI UI at `http://localhost:8080/swagger-ui.html`.

Demo accounts are seeded on backend startup:

- `demo@skillswap.local` / `Password123!` - learner with an upcoming UI/UX session
- `ananya@skillswap.local` / `Password123!` - UI/UX teacher
- `rohan@skillswap.local` / `Password123!` - Python teacher
- `maya@skillswap.local` / `Password123!` - Photography teacher

The demo account has reciprocal skills, a match, a future accepted session and wallet activity so the main workflow can be demonstrated immediately.

## Executable demo workflow

1. Open `http://localhost:5173`.
2. Select **Log in** and use `demo@skillswap.local` / `Password123!`.
3. Open **Discover**. Search for `UI/UX`, then select **Start a session** on Ananya Sharma.
4. Choose a future date, start time, and either 30 minutes (1 credit) or 60 minutes (2 credits). Add a message and select **Send session request**.
5. Open **Sessions** to see the new request alongside the seeded accepted session.
6. Open **Credit wallet** to see the introductory credit ledger.
7. Log out and sign in as `ananya@skillswap.local` / `Password123!` to demonstrate the teacher-side account.

The splash page explains this same flow visually. New accounts receive 5 introductory credits and can immediately search students and request a session. A session request is stored with `REQUESTED` status; accepted demo sessions are seeded for presentation.

## Structure

- `backend/` - Java 21, Spring Boot, JWT authentication, JDBC, Flyway, MySQL
- `frontend/` - React, TypeScript, Vite, Tailwind, React Query
- `.github/workflows/ci.yml` - build and test workflow
