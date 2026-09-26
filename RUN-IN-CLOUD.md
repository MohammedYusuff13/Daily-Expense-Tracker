# Run in the cloud (no local install)

You don't need an IDE, Java, or Node on your own machine. Push this project to
GitHub, then open it in a browser-based dev environment. The `.devcontainer/`
config installs Java 17, Maven, and Node 18 for you automatically.

## Step 1 — Put the code on GitHub

From the unzipped project folder:

```bash
cd daily-expense-tracker
git init
git add .
git commit -m "Initial commit: ShopBook expense tracker"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

Create the empty `<your-repo>` on github.com first (no README/gitignore — this
project already has them).

## Step 2 — Open in GitHub Codespaces

1. On your repo page, click the green **Code** button → **Codespaces** tab →
   **Create codespace on main**.
2. Wait for it to build. It reads `.devcontainer/devcontainer.json`, installs
   Java + Node, and runs `npm install` in `frontend/` for you.
3. When the terminal is ready, start both servers with one command:

   ```bash
   ./start.sh
   ```

   This launches the backend on port 8080 (logs go to `backend.log`), waits for
   it to come up, then starts the frontend on port 3000.

4. Codespaces auto-forwards port 3000 and opens a preview. If it doesn't, open
   the **Ports** tab and click the globe icon next to port 3000.

That's it — the app is running in your browser, backed by the Excel workbook the
backend creates at `backend/data/expense-tracker.xlsx`.

### Prefer to run them separately?

Open two terminals in the Codespace:

```bash
# Terminal 1 — backend
cd backend && mvn spring-boot:run

# Terminal 2 — frontend
cd frontend && npm run dev
```

## Gitpod alternative

Gitpod also reads the same `.devcontainer/` config. Prefix your repo URL:
`https://gitpod.io/#https://github.com/<your-username>/<your-repo>`, then run
`./start.sh` in its terminal.

## Notes

- Start the backend before (or together with) the frontend — the frontend
  proxies `/api` calls to `localhost:8080` inside the same machine, so no URLs
  need changing.
- First backend start is slower because Maven downloads dependencies; later
  starts are fast.
- The forwarded port 3000 URL is private to your account by default. To share a
  demo, set port 3000 to **Public** in the Ports tab.
