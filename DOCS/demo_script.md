# TraceIntel Demo Script

Time: about 5 minutes. The app starts empty, so every record is entered live. Full steps and sample values are in `how_to_use.md`.

The sample parts already have their cost, scrap, rework and parts per vehicle.

| Scene | Login | What to do | What to say |
|---|---|---|---|
| 1. Receive | `supervisor1` | Add a material lot | "Every delivery gets a lot number, so we always know the supplier." |
| 2. Produce | `supervisor1` | Start a batch, link the lot with the material used, generate serials | "This link is what makes tracing possible." |
| 3. Scan | `operator1` | Scan a correct serial, the same serial, then a serial from another part | "The right part passes. A repeat or wrong part is blocked and recorded." |
| 4. Ship | `supervisor1`, `admin` | Create a shipment, add the batch, link a VIN | "Now we know the customer and the vehicle." |
| 5. Hold | `priya`, `vikram` | Record a failed inspection, request a hold, try to self-approve, then approve as manager | "One person cannot place a hold alone." |
| 6. Block | `operator1`, `supervisor1` | Scan a new serial, try to ship the batch again | "The held lot is stopped at scan and at shipping." |
| 7. Trace | `vikram` | Run forward trace, then backward trace with the VIN | "One click shows the impact. The time is measured and saved." |
| 8. Release | `vikram` | Release the lot with a reason, open the audit log | "If the material passes a retest, it can be released, and every action is recorded." |

All demo accounts use the password `pass123`. The data is demonstration data, not real company data.
