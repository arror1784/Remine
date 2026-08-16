import { API_BASE_URL } from '../playwright.config'

// The suite asserts against real backend responses, so a missing backend must
// fail loudly here rather than as five separate "element not found" timeouts.
export default async function globalSetup() {
  let status: number
  try {
    const response = await fetch(`${API_BASE_URL}/actuator/health`)
    status = response.status
  } catch (cause) {
    throw new Error(
      `E2E backend is not reachable at ${API_BASE_URL}. Start it with:\n` +
        `  cd Remine_backend && JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app-api:bootRun\n` +
        `(redis must be running too: redis-cli ping)\n\nCause: ${String(cause)}`
    )
  }
  if (status !== 200) {
    throw new Error(`E2E backend at ${API_BASE_URL} answered /actuator/health with ${status}, expected 200.`)
  }
}
