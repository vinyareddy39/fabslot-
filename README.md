# FabSlot — Fab Lab Equipment & Slot Booking System

A full-stack system for booking time slots on shared fabrication-lab machines
(CNC routers, 3D printers, laser cutters), replacing informal WhatsApp/register
based scheduling with conflict-checked bookings, admin approval, and basic
usage analytics.

## Stack
- Backend: Java 17, Spring Boot 3.3 (Web, Data JPA, Security), MySQL, JWT (jjwt)
- Frontend: React 18 + Vite, React Router

## Problem Statement
College fab labs are shared informally, causing double-bookings, no usage
history, and no visibility into machine downtime or utilization.

## Solution
Students request a machine + time slot. The backend rejects requests that
overlap an already-approved booking on the same machine. Lab admins approve
or reject pending requests, manage machine status (available/maintenance/
retired), and mark completed sessions, which are logged for analytics
(bookings per machine, hours used, most active students).

## Roles
- `STUDENT` — browse machines, request/cancel own bookings, view own history
- `LAB_ADMIN` — manage machines, approve/reject/complete bookings, view analytics

---

## 1. Database Setup (MySQL)

You just need MySQL running locally — the schema is created automatically by
Hibernate (`ddl-auto=update`) the first time the backend starts.

```sql
CREATE DATABASE IF NOT EXISTS fabslot_db;
```

Update `backend/src/main/resources/application.properties` with your own
MySQL username/password:

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

## 2. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend starts on **http://localhost:8080**.

## 3. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on **http://localhost:5173**.

## 4. Try it out

1. Go to `http://localhost:5173/register`, create a `LAB_ADMIN` account and a
   `STUDENT` account.
2. Log in as `LAB_ADMIN` → **Machines** tab → add a few machines (e.g.
   "CNC-01", type CNC Router, status Available).
3. Log out, log in as the `STUDENT` account → book a slot on a machine.
4. Log back in as `LAB_ADMIN` → **Pending Requests** → Approve it.
5. Check the **Analytics** tab after marking a booking complete under
   **All Bookings**.

## API Overview

| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | `/auth/register` | public | create account |
| POST | `/auth/login` | public | get JWT |
| GET | `/api/machines` | any logged-in | list machines |
| POST/PUT/DELETE | `/api/machines/**` | LAB_ADMIN | manage machines |
| POST | `/api/bookings` | STUDENT | request a slot |
| GET | `/api/bookings/mine` | any logged-in | own bookings |
| GET | `/api/bookings`, `/pending` | LAB_ADMIN | view queues |
| PUT | `/api/bookings/{id}/approve\|reject\|complete` | LAB_ADMIN | manage a booking |
| DELETE | `/api/bookings/{id}` | owner | cancel own booking |
| GET | `/api/admin/analytics/**` | LAB_ADMIN | usage stats |

## Notes on originality
This project was built from scratch as an original implementation. It uses
different entity design (`Member`/`Machine`/`Booking`/`UsageLog`), package
naming (`com.fabslot.*`), and authentication approach (stateless JWT) than any
reference/starter project it was inspired by.
