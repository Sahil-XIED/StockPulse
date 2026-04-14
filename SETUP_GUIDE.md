# ═══════════════════════════════════════════════════════════
#  StockPulse — Complete Fullstack Setup Guide
#  Senior Full-Stack Developer Reference
# ═══════════════════════════════════════════════════════════

## PROJECT OVERVIEW
A professional stock trading simulation platform built with:
- Frontend  : HTML + CSS + JavaScript (VS Code + Live Server)
- Backend   : Java 17 + Spring Boot 3.2 + Spring Security + JWT
- Database  : MySQL 8.x
- Build Tool: Maven

## COMPLETE FOLDER STRUCTURE
```
StockPulse-Fullstack/
│
├── backend/                          ← Eclipse Maven Project
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/stockpulse/
│       │   │   ├── StockPulseApplication.java     ← Main entry point
│       │   │   ├── config/
│       │   │   │   └── SecurityConfig.java         ← JWT + CORS + BCrypt
│       │   │   ├── controller/
│       │   │   │   └── Controllers.java            ← All REST controllers
│       │   │   ├── dto/
│       │   │   │   └── Dtos.java                   ← Request/Response DTOs
│       │   │   ├── exception/
│       │   │   │   ├── StockPulseException.java
│       │   │   │   └── GlobalExceptionHandler.java
│       │   │   ├── model/
│       │   │   │   ├── User.java
│       │   │   │   ├── Stock.java
│       │   │   │   ├── Order.java
│       │   │   │   ├── Portfolio.java
│       │   │   │   ├── Watchlist.java
│       │   │   │   └── PriceHistory.java
│       │   │   ├── repository/
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── StockRepository.java
│       │   │   │   └── Repositories.java           ← Order, Portfolio, Watchlist, PriceHistory
│       │   │   ├── scheduler/
│       │   │   │   └── StockPriceScheduler.java    ← Live price simulation
│       │   │   ├── security/
│       │   │   │   ├── JwtUtil.java
│       │   │   │   └── JwtAuthFilter.java          ← + UserDetailsServiceImpl
│       │   │   └── service/
│       │   │       ├── AuthService.java
│       │   │       ├── StockService.java            ← + seed 25 NSE stocks
│       │   │       ├── OrderService.java
│       │   │       ├── PortfolioService.java
│       │   │       └── WatchlistService.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│
├── frontend/                         ← VS Code Project
│   ├── index.html                    ← Full dashboard SPA
│   ├── login.html
│   ├── signup.html
│   ├── css/
│   │   └── style.css                 ← Master stylesheet (1,200+ lines)
│   └── js/
│       ├── api.js                    ← API client + Mock data engine
│       └── dashboard.js             ← All section rendering + Chart.js
│
├── database/
│   └── schema.sql                    ← MySQL schema + seed data
│
└── SETUP_GUIDE.md                    ← This file
```

═══════════════════════════════════════════════════════════
  STEP 1 — PREREQUISITES (Install These First)
═══════════════════════════════════════════════════════════

Install the following software before starting:

1. Java 17 JDK
   Download: https://adoptium.net/
   Verify  : java -version   (should show 17.x.x)

2. Eclipse IDE for Enterprise Java Developers
   Download: https://www.eclipse.org/downloads/
   Choose  : "Eclipse IDE for Enterprise Java and Web Developers"

3. Maven 3.8+
   Eclipse includes Maven by default.
   Verify  : mvn -version

4. MySQL 8.x
   Download: https://dev.mysql.com/downloads/installer/
   Also install: MySQL Workbench (for GUI)

5. VS Code
   Download: https://code.visualstudio.com/
   Extension: "Live Server" by Ritwick Dey (search in Extensions panel)

6. Postman (optional, for API testing)
   Download: https://www.postman.com/downloads/


═══════════════════════════════════════════════════════════
  STEP 2 — DATABASE SETUP (MySQL)
═══════════════════════════════════════════════════════════

OPTION A — Using MySQL Workbench (Recommended for beginners)
─────────────────────────────────────────────────────────
1. Open MySQL Workbench
2. Connect to your local MySQL server (host: localhost, port: 3306)
3. Click File → Open SQL Script
4. Select: StockPulse-Fullstack/database/schema.sql
5. Click the lightning bolt icon (⚡) to execute
6. You should see:
   status: "Setup complete!"
   total_users: 2
   total_stocks: 25

OPTION B — Using MySQL Command Line
─────────────────────────────────────────────────────────
Open terminal and run:

  mysql -u root -p < /path/to/StockPulse-Fullstack/database/schema.sql

Or manually:
  mysql -u root -p
  source /path/to/StockPulse-Fullstack/database/schema.sql;

VERIFY — Run these queries to confirm setup:
─────────────────────────────────────────────────────────
  USE stockpulse;
  SELECT id, name, email, role FROM users;
  SELECT symbol, company_name, price FROM stocks LIMIT 5;

Expected output:
  Users: admin@stockpulse.com (ADMIN), demo@stockpulse.com (USER)
  Stocks: RELIANCE, TCS, HDFCBANK, INFY, ICICIBANK ... (25 total)


═══════════════════════════════════════════════════════════
  STEP 3 — BACKEND SETUP IN ECLIPSE
═══════════════════════════════════════════════════════════

3.1 Import Project into Eclipse
────────────────────────────────
1. Open Eclipse
2. Go to: File → Import → Maven → Existing Maven Projects
3. Browse to: StockPulse-Fullstack/backend/
4. Select pom.xml → Click Finish
5. Eclipse will download all Maven dependencies (takes 2–5 minutes)
   Watch the progress bar at the bottom of Eclipse

3.2 Configure MySQL Password
────────────────────────────
Open: src/main/resources/application.properties

Change this line to match YOUR MySQL setup:
  spring.datasource.password=root      ← put your MySQL root password

Also verify:
  spring.datasource.url=jdbc:mysql://localhost:3306/stockpulse?...
  spring.datasource.username=root

3.3 Enable Lombok in Eclipse
────────────────────────────
Lombok requires a one-time setup in Eclipse:

1. Find lombok.jar in your Maven repository:
   Windows: C:\Users\YourName\.m2\repository\org\projectlombok\lombok\1.18.x\lombok-1.18.x.jar
   Mac/Linux: ~/.m2/repository/org/projectlombok/lombok/1.18.x/

2. Double-click the lombok.jar file
3. The Lombok installer will open → click "Install / Update"
4. Restart Eclipse

Alternative (if Lombok causes issues):
   In pom.xml, the Lombok annotations generate getters/setters automatically.
   If Eclipse shows red errors on @Data, @Builder etc., try:
   Right-click project → Maven → Update Project → OK

3.4 Run the Spring Boot Application
────────────────────────────────────
1. In Eclipse Package Explorer, find:
   src/main/java/com/stockpulse/StockPulseApplication.java

2. Right-click → Run As → Java Application

3. Watch the console — you should see:
   ======================================
     StockPulse Backend Started! 🚀
     API Base: http://localhost:8080/api
   ======================================

4. Also watch for:
   "Seeding stock data..." → 25 stocks inserted
   "Seeded user: demo@stockpulse.com (USER)"
   "Seeded user: admin@stockpulse.com (ADMIN)"

3.5 Verify Backend is Running
────────────────────────────────
Open browser and visit:
  http://localhost:8080/api/stocks         → Should return JSON array of stocks
  http://localhost:8080/actuator/health    → Should return {"status":"UP"}

If you see a JSON response → backend is working!


═══════════════════════════════════════════════════════════
  STEP 4 — FRONTEND SETUP IN VS CODE
═══════════════════════════════════════════════════════════

4.1 Open Frontend in VS Code
────────────────────────────
1. Open VS Code
2. File → Open Folder → Select: StockPulse-Fullstack/frontend/
3. You will see: index.html, login.html, signup.html, css/, js/

4.2 Install Live Server Extension
────────────────────────────────
1. Click Extensions icon (or Ctrl+Shift+X)
2. Search: "Live Server"
3. Install: Live Server by Ritwick Dey
4. Restart VS Code

4.3 Launch the Frontend
────────────────────────────
1. Right-click login.html in the Explorer panel
2. Click "Open with Live Server"
3. Browser opens at: http://127.0.0.1:5500/login.html

4.4 Login Credentials
────────────────────────────
Demo User  : demo@stockpulse.com  / demo123
Admin User : admin@stockpulse.com / admin123

Note: If Spring Boot is NOT running, the frontend uses mock data
automatically and still works fully for demo purposes.


═══════════════════════════════════════════════════════════
  STEP 5 — CONNECTING FRONTEND TO SPRING BOOT
═══════════════════════════════════════════════════════════

The frontend is already configured to connect to Spring Boot.
Here's how it works:

In js/api.js:
  const BASE = 'http://localhost:8080/api';

Every API call:
1. Sends JWT token in Authorization header
2. Falls back to MOCK data if backend is unavailable
3. Saves session to localStorage on login

Test the connection:
1. Make sure Spring Boot is running on :8080
2. Open frontend at http://127.0.0.1:5500/login.html
3. Login with demo@stockpulse.com / demo123
4. Open browser DevTools → Network tab
5. You should see calls to localhost:8080/api/stocks returning real data


═══════════════════════════════════════════════════════════
  STEP 6 — API ENDPOINTS REFERENCE
═══════════════════════════════════════════════════════════

BASE URL: http://localhost:8080/api

PUBLIC (no JWT needed):
  POST   /auth/signup                  → Register new user
  POST   /auth/login                   → Login, returns JWT
  GET    /stocks                       → List all 25 NSE stocks
  GET    /stocks/{symbol}              → Get one stock (e.g. /stocks/RELIANCE)
  GET    /stocks/search?q=tata         → Search stocks
  GET    /stocks/{symbol}/history      → Price history for chart
  GET    /stocks/gainers               → Top gaining stocks
  GET    /stocks/losers                → Top losing stocks

PROTECTED (JWT required in Authorization: Bearer <token>):
  POST   /orders/buy                   → Buy stock
  POST   /orders/sell                  → Sell stock
  GET    /orders/user/{userId}         → User's order history
  GET    /portfolio/{userId}           → Portfolio with P&L
  GET    /watchlist/{userId}           → Get watchlist
  POST   /watchlist/add                → Add to watchlist
  DELETE /watchlist/{userId}/{symbol}  → Remove from watchlist

ADMIN only (role: ADMIN):
  GET    /admin/users                  → All registered users
  GET    /admin/stats                  → Platform statistics

SAMPLE REQUEST BODIES:
─────────────────────

POST /api/auth/signup
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "Test@1234",
  "riskProfile": "MODERATE"
}

POST /api/auth/login
{
  "email": "demo@stockpulse.com",
  "password": "demo123"
}

POST /api/orders/buy
{
  "userId": 2,
  "symbol": "RELIANCE",
  "quantity": 10
}

POST /api/orders/sell
{
  "userId": 2,
  "symbol": "TCS",
  "quantity": 5
}

POST /api/watchlist/add
{
  "userId": 2,
  "symbol": "HDFCBANK"
}


═══════════════════════════════════════════════════════════
  STEP 7 — TESTING WITH POSTMAN
═══════════════════════════════════════════════════════════

7.1 Test Login
──────────────
Method : POST
URL    : http://localhost:8080/api/auth/login
Headers: Content-Type: application/json
Body   :
{
  "email": "demo@stockpulse.com",
  "password": "demo123"
}

Expected Response:
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "id": 2,
    "name": "Demo Trader",
    "email": "demo@stockpulse.com",
    "role": "USER",
    "balance": 100000.00
  }
}

7.2 Copy the token from response

7.3 Test Protected Endpoint
──────────────────────────────
Method : GET
URL    : http://localhost:8080/api/portfolio/2
Headers:
  Content-Type: application/json
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...   ← paste your token

7.4 Test Buy Order
──────────────────
Method : POST
URL    : http://localhost:8080/api/orders/buy
Headers: Authorization: Bearer <token>
Body   :
{
  "userId": 2,
  "symbol": "RELIANCE",
  "quantity": 5
}


═══════════════════════════════════════════════════════════
  STEP 8 — LIVE PRICE SIMULATION
═══════════════════════════════════════════════════════════

The StockPriceScheduler runs every 5 seconds and:
1. Randomly fluctuates all stock prices by ±0.7%
2. Updates changePercent relative to previousClose
3. Saves a PriceHistory snapshot for charting

You can see this in Eclipse console:
  DEBUG: Price simulation tick completed for 25 stocks

The frontend polls GET /api/stocks every 5 seconds and:
1. Updates the live ticker tape
2. Re-renders the top movers
3. Updates portfolio P&L in real-time


═══════════════════════════════════════════════════════════
  STEP 9 — COMMON ERRORS & FIXES
═══════════════════════════════════════════════════════════

ERROR: "Access to fetch at localhost:8080 blocked by CORS"
FIX  : Make sure Spring Boot is running AND you're using Live Server
       (http://127.0.0.1:5500 not file://)
       CORS is configured for both localhost:5500 and 127.0.0.1:5500

ERROR: "Communications link failure" (MySQL connection refused)
FIX  : MySQL service is not running.
       Windows: Services → MySQL80 → Start
       Mac    : System Preferences → MySQL → Start MySQL Server
       Linux  : sudo service mysql start

ERROR: "Unknown column" or "Table doesn't exist"
FIX  : Run schema.sql again in MySQL Workbench
       Or set spring.jpa.hibernate.ddl-auto=create in application.properties
       (use 'update' for production)

ERROR: Cannot find symbol @Data, @Builder (Lombok)
FIX  : Install Lombok in Eclipse (see Step 3.3)
       Or run: Right-click project → Maven → Update Project

ERROR: Port 8080 already in use
FIX  : Either stop the other process or change port:
       In application.properties: server.port=9090
       Then update api.js: const BASE = 'http://localhost:9090/api';

ERROR: 401 Unauthorized on API calls
FIX  : JWT token expired or missing.
       Log out and log back in to get a fresh token.
       Token validity: 24 hours (configurable in application.properties)

ERROR: "Insufficient balance" on buy order
FIX  : Demo user starts with ₹1,00,000.
       Try buying fewer shares or a cheaper stock.
       Admin user has ₹99,99,999 balance.


═══════════════════════════════════════════════════════════
  STEP 10 — PROJECT ARCHITECTURE DIAGRAM
═══════════════════════════════════════════════════════════

  ┌─────────────────────────────────────────────────────┐
  │           BROWSER (VS Code Live Server :5500)        │
  │                                                      │
  │  login.html  signup.html  index.html                 │
  │      │           │            │                      │
  │  js/api.js ──────────────────┘                      │
  │      │         (fetch with JWT)                      │
  └──────┼──────────────────────────────────────────────┘
         │ HTTP REST (JSON)
         ▼
  ┌─────────────────────────────────────────────────────┐
  │           SPRING BOOT (:8080)                        │
  │                                                      │
  │  Controllers → Services → Repositories              │
  │       │                                              │
  │  Security:  JWT Filter → BCrypt Auth                 │
  │  Scheduler: Price tick every 5 seconds              │
  └──────┼──────────────────────────────────────────────┘
         │ JPA / Hibernate (SQL)
         ▼
  ┌─────────────────────────────────────────────────────┐
  │           MYSQL (:3306)                              │
  │                                                      │
  │  users  stocks  orders  portfolio  watchlist         │
  │  price_history                                       │
  └─────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════
  DEFAULT CREDENTIALS SUMMARY
═══════════════════════════════════════════════════════════

  Role   Email                      Password    Balance
  ─────  ─────────────────────────  ──────────  ──────────────
  ADMIN  admin@stockpulse.com       admin123    ₹99,99,999
  USER   demo@stockpulse.com        demo123     ₹1,00,000


═══════════════════════════════════════════════════════════
  FEATURES CHECKLIST
═══════════════════════════════════════════════════════════

Frontend:
  ✅ Login page with JWT authentication
  ✅ 4-step signup wizard with risk profile
  ✅ Live ticker tape (auto-scrolling)
  ✅ Dashboard with market sentiment + AI picks
  ✅ Trade page with Chart.js price chart
  ✅ Buy/Sell order form with total calculator
  ✅ Portfolio with real-time P&L + sector allocation
  ✅ Watchlist with add/remove and AI signals
  ✅ Stocks page with 25 NSE stocks + gainers/losers filter
  ✅ Commodity page (Gold, Silver, Crude, Gas + 5 more)
  ✅ F&O page (Options chain, Futures table, PCR, VIX)
  ✅ IPO page with 4 current IPOs + GMP + AI rating
  ✅ Mutual Funds page (6 funds, SIP investing)
  ✅ Orders history table
  ✅ Profile page with preferences toggles
  ✅ Quick trade modal (from any page)
  ✅ Add-to-watchlist search modal
  ✅ Toast notification system
  ✅ Responsive design (mobile-friendly)
  ✅ Mock data fallback when backend is offline

Backend:
  ✅ JWT authentication (signup + login)
  ✅ BCrypt password hashing (strength 12)
  ✅ Role-based access (USER / ADMIN)
  ✅ 25 NSE stocks auto-seeded on startup
  ✅ Stock price simulation (every 5 seconds)
  ✅ Buy/sell with balance & quantity validation
  ✅ Portfolio tracking with weighted average price
  ✅ Real-time P&L calculation
  ✅ Persistent watchlist (DB stored)
  ✅ Order history with timestamps
  ✅ CORS configured for Live Server
  ✅ Global exception handling with consistent JSON errors
  ✅ Spring Data JPA with MySQL 8

Database:
  ✅ 6 tables (users, stocks, orders, portfolio, watchlist, price_history)
  ✅ Foreign keys + unique constraints
  ✅ Indexed for performance
  ✅ Pre-seeded with 25 real NSE stocks + 2 default users
