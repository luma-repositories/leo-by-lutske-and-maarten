
# Prompt
You are a senior Java/Quarkus engineer. I have a legacy static website that is currently served via a simple Python HTTP server (e.g., `python -m http.server`). I want to migrate it to a Quarkus application so that:

1) The website is still served as static assets by Quarkus (HTML/CSS/JS/images).
2) The website can call backend APIs served by Quarkus.
3) For now, implement ONE backend API endpoint that returns a hard-coded version value.
4) The version value must be configurable via `application.properties`.
5) The webpage must display that version by calling the Quarkus REST endpoint using jQuery (AJAX/fetch via jQuery).
6) Starting the website should be done via a Quarkus command (`./mvnw quarkus:dev` for dev; and document how to run packaged mode too).

Constraints / preferences:
- Keep the existing site structure and files as much as possible.
- Do not introduce a frontend framework; keep it “static site + jQuery”.
- Use Quarkus REST (Jakarta REST / RESTEasy Reactive).
- Ensure CORS is not needed by serving the page and API from the same Quarkus origin.
- Provide clear file-level changes and exact commands to run.

Tasks:
A) Create a new Quarkus project (Maven) in the repo (or convert the repo) and place the existing static site under `src/main/resources/META-INF/resources/` so it is served at `/` by Quarkus.
- Ensure `index.html` is reachable at `http://localhost:8080/`.

B) Add a REST endpoint:
- Path: `/api/version`
- Method: GET
- Response: JSON like `{ "version": "1.2.3" }`
- The `"1.2.3"` must come from `application.properties` key, e.g. `app.version=1.2.3`.
- Implement this using configuration injection (MicroProfile Config): `@ConfigProperty(name="app.version")`.

C) Update the webpage:
- In `index.html` (or whichever entry page exists), add a placeholder element (e.g., `<span id="appVersion"></span>`).
- Add jQuery code that calls `GET /api/version` and renders the value into that element.
- Handle error case gracefully (e.g., show “unknown” if request fails).

D) Provide the “how to run”:
- Dev mode: `./mvnw quarkus:dev` (or `mvn quarkus:dev`)
- Packaged: `./mvnw package` then `java -jar target/quarkus-app/quarkus-run.jar`
- Mention the URLs to open and test: `/` and `/api/version`.

E) Deliverables:
- List all created/modified files with their contents.
- Include the Quarkus dependencies in `pom.xml` (at minimum: REST, config).
- If any existing paths/assets break due to relative URLs, fix them while keeping structure intact.

Repository context I will provide:
- The existing static site folder structure (HTML/CSS/JS/assets).
- You should infer which file is the entry point (likely `index.html`) and preserve references.

Now proceed: implement the migration and output a step-by-step summary + the exact diffs/content for each changed file.