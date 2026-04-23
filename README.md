# DingerIO
A customizable MLB live game notification system that polls the official MLB Stats API and delivers real-time updates to Discord based on user-defined preferences.

---

## Tech Stack
- **Java / Spring Boot** — backend application and scheduling
- **PostgreSQL** — persistent storage for users, teams, players, and subscriptions
- **MLB Stats API** — source for live game data and roster information
- **Discord Webhooks** — delivery channel for real-time notifications

---

## How It Works
DingerIO polls live MLB game data every 30 seconds and compares it against a stored game state to detect changes. When a tracked event occurs such as an inning change, score update, or home run, a notification is sent to the user's configured Discord webhook.

Users can subscribe to specific teams or players and choose exactly which in-game events they want to be notified about, allowing for fully personalized updates.
