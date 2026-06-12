## Reviora - Intelligent DSA Analysis Platform

> Algorithm Engineering Operating System — not just a code runner.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + TypeScript + Vite |
| Styling | Tailwind CSS v4 + CSS Variables |
| Editor | Monaco Editor |
| Charts | Recharts |
| State | Zustand |
| Data Fetching | TanStack Query |
| Backend | Spring Boot 3.2 (Java 21) |
| Security | Spring Security + JWT |
| Database | PostgreSQL (Neon) |
| Cache | Redis |
| Migrations | Flyway |
| Code Execution | Piston API |
| AI Review | OpenRouter (Claude/GPT) |
| Frontend Deploy | Vercel |
| Backend Deploy | Render |

---

## Project Structure

```
reviora/
├── frontend/               # React + Vite + TypeScript
│   ├── src/
│   │   ├── app/
│   │   ├── pages/          # HomePage, LoginPage, RegisterPage, WorkspacePage, DashboardPage
│   │   ├── layouts/        # AppLayout, PublicLayout
│   │   ├── modules/        # editor/, execution/, analytics/, ai-review/
│   │   ├── components/     # ui/, shared/
│   │   ├── services/       # api.ts (Axios client)
│   │   ├── store/          # Zustand stores (auth, editor, theme)
│   │   └── types/
│   ├── vercel.json
│   └── .env.example
├── backend/                # Spring Boot 3.2
│   ├── src/main/java/com/reviora/
│   │   ├── controller/     # AuthController, SubmissionController, AIController, AnalyticsController
│   │   ├── service/        # AuthService, PistonService, ComplexityAnalysisEngine, AIService
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Spring Data JPA repos
│   │   ├── security/       # JwtTokenProvider, JwtAuthenticationFilter
│   │   ├── config/         # SecurityConfig, AppConfig
│   │   ├── dto/            # Request/response DTOs
│   │   └── exception/      # Custom exceptions + GlobalExceptionHandler
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/   # Flyway SQL migrations
│   └── .env.example
├── render.yaml             # Render deployment config
└── README.md
```

---

## Local Development

### Prerequisites
- Node.js 20+
- Java 21+
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+

### Frontend

```bash
cd frontend
cp .env.example .env
# Edit .env with your API URL
npm install
npm run dev
# → http://localhost:5173
```

### Backend

```bash
cd backend
cp .env.example .env
# Edit .env with your DB credentials

# Create local database
psql -U postgres -c "CREATE DATABASE reviora;"

# Run with Maven
mvn spring-boot:run

# → http://localhost:8080
# → Swagger: http://localhost:8080/swagger-ui.html
```

---

## Environment Variables

### Frontend (`frontend/.env`)
```
VITE_API_URL=http://localhost:8080
VITE_GOOGLE_CLIENT_ID=your-google-client-id
```

### Backend (`backend/.env` or Render env vars)
```
SPRING_DATASOURCE_URL=jdbc:postgresql://host/reviora?sslmode=require
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password
JWT_SECRET=your-32-char-minimum-secret-key
REDIS_URL=redis://localhost:6379
PISTON_API_URL=https://emkc.org/api/v2/piston
OPENROUTER_API_KEY=sk-or-v1-...
GOOGLE_CLIENT_ID=xxx.apps.googleusercontent.com
CORS_ORIGINS=https://your-app.vercel.app
```

---

## Deployment

### 1. Database — Neon PostgreSQL

1. Sign up at [neon.tech](https://neon.tech)
2. Create a new project → `reviora`
3. Copy the connection string
4. Flyway will run migrations automatically on first start

### 2. Backend — Render

1. Push code to GitHub
2. Go to [render.com](https://render.com) → New → Web Service
3. Connect your GitHub repo
4. Configure:
   - **Runtime:** Java
   - **Build Command:** `cd backend && mvn clean package -DskipTests`
   - **Start Command:** `java -jar backend/target/reviora-backend-1.0.0.jar`
   - **Health Check:** `/api/health`
5. Add all environment variables from the list above
6. Also create a Redis instance on Render (free tier)

### 3. Frontend — Vercel

1. Go to [vercel.com](https://vercel.com) → New Project
2. Import your GitHub repo
3. Configure:
   - **Framework:** Vite
   - **Root Directory:** `frontend`
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`
4. Add environment variables:
   - `VITE_API_URL` = your Render backend URL
   - `VITE_GOOGLE_CLIENT_ID` = your Google client ID

---

## Features

### Complexity Analysis Engine (No AI)
- AST-level static analysis in Java
- Detects: nested loops, recursion, HashMap usage, divide-and-conquer
- Maps to: O(1), O(log n), O(n), O(n log n), O(n²), O(n³), O(2^n)

### Runtime Growth Visualization
- Computes theoretical operations at n = 10, 50, 100, 500, 1000, 5000, 10000
- Renders interactive Recharts line chart

### Piston API Integration
- Supports: C++, C, Java, Python, JavaScript, TypeScript, Go, Rust
- 5-second execution timeout
- Captures stdout, stderr, exit code, execution time

### AI Review (OpenRouter)
- Sends code + detected complexity to Claude/GPT
- Returns: current approach, optimization, expected complexity, interview notes
- Gracefully falls back if API key not configured

### Multi-Theme System
- Graphite (dark professional)
- Aurora (deep blue futuristic)
- Ivory (clean light)
- Matrix (terminal green)
- All via CSS custom properties — zero extra JS

### Authentication
- JWT access token (24h) + refresh token (7 days)
- Google OAuth via Google Identity Services
- Refresh token rotation + cleanup
- Role-based access (USER / ADMIN)

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | ✗ | Register |
| POST | `/api/auth/login` | ✗ | Login |
| POST | `/api/auth/google` | ✗ | Google OAuth |
| POST | `/api/auth/refresh` | ✗ | Refresh token |
| GET | `/api/auth/me` | ✓ | Current user |
| POST | `/api/submissions` | ✓ | Submit + execute |
| POST | `/api/submissions/guest` | ✗ | Guest submit |
| POST | `/api/submissions/:id/analyze` | ✓ | Complexity analysis |
| GET | `/api/submissions/:id/growth` | ✓ | Growth data |
| GET | `/api/submissions/me` | ✓ | My submissions |
| POST | `/api/ai/review/:id` | ✓ | AI review |
| POST | `/api/ai/tests/:id` | ✓ | Test generation |
| GET | `/api/analytics/dashboard` | ✓ | Dashboard |

Full Swagger docs at `/swagger-ui.html`

---

## License

MIT © Reviora
