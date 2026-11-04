# TraceIntel AG-1

Manufacturing traceability for an auto-parts plant. It blocks wrong parts at the scan station and traces a material lot forward to shipments and VINs, or a VIN back to its supplier.

**Stack:** React, Framer Motion, Three.js, Java 21, Spring Boot 3.4, Spring Security, JWT, PostgreSQL, Flyway, ZXing, html5-qrcode, JUnit, Mockito.

## Folders

| Folder | What it holds |
|---|---|
| `backend/` | Spring Boot API and Flyway migrations |
| `frontend/` | React application |
| `database/` | Database notes |
| `DOCS/` | Start with `DOCS/how_to_use.md`. Also the screen guide (`user_guide.md`), common questions (`faq.md`) and plans |

## Run locally

You need JDK 21, Maven, Node.js and PostgreSQL. Create an empty `traceintel` database, then set these environment variables:

| Variable | Example |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/traceintel` |
| `SPRING_DATASOURCE_USERNAME` | Your database user |
| `SPRING_DATASOURCE_PASSWORD` | Your database password |
| `APP_JWT_SECRET` | A random value of at least 32 characters |

```powershell
cd backend
mvn clean package
java -jar target/traceintel-ag1-1.0.0.jar
```

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Flyway creates the tables and data when the backend starts.

## Deploy

Netlify hosts the frontend. Render hosts the backend and PostgreSQL.

**Render (do this first):**

1. In Render, choose **New > Blueprint** and pick this repository. `render.yaml` creates the database and the backend. The backend is built from `backend/Dockerfile`, because Render has no built-in Java runtime.
2. When Render asks for `APP_CORS_ALLOWED_ORIGINS`, enter the Netlify address, for example `https://your-site.netlify.app`. If you do not know it yet, enter a placeholder and change it later.
3. Wait for the deploy. Flyway creates the tables and data. Copy the backend address, for example `https://traceintel-backend.onrender.com`.

Render fills in the database settings (`DB_HOST`, `DB_PORT`, `DB_NAME`, user, password) and generates `APP_JWT_SECRET`. On the free plan, the backend sleeps when idle, so the first request can take about a minute. A free Render database expires after a limited time.

**Netlify:**

1. Import the repository. `netlify.toml` already sets the build: base `frontend`, command `npm run build`, publish `dist`.
2. Under **Site configuration > Environment variables**, add `VITE_API_BASE_URL` with the Render address plus `/api`, for example `https://traceintel-backend.onrender.com/api`. See `frontend/.env.example`.
3. Deploy. After any change to that variable, deploy again, because the value is built into the site.
4. If the Netlify address is new, put it in `APP_CORS_ALLOWED_ORIGINS` on Render and redeploy the backend.

## Data

The demo accounts all use the password `pass123`. Change them before any real use.

The app starts with no lots, batches, scans or shipments. They are entered live. No data here is real company data.

Master data comes from public Kaggle datasets:

- [Supply Chain Datasets](https://www.kaggle.com/datasets/ayodejiibrahimlateef/supply-chain-datasets), CC BY 4.0
- [Auto Parts Dataset](https://www.kaggle.com/datasets/qubdidata/auto-parts-dataset), CC BY-NC 4.0, so it cannot be used commercially
- [Vehicle Sales Data](https://www.kaggle.com/datasets/syedanwarafridi/vehicle-sales-data), MIT

## Tests

```powershell
cd backend
mvn test
```
