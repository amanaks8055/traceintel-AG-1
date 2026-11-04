# TraceIntel: User Guide

Yeh guide har screen ki details batati hai. Pehli baar app use kar rahe ho to `how_to_use.md` se shuru karo.

> Is app ka data real company data nahi hai. Suppliers, parts, customers aur VINs Kaggle datasets aur kuch demo records se aaye hain. Baaki data demo ke samay live enter hota hai.

---

## 1. App kya problem solve karti hai

**Problem 1: Galat part line par lag jata hai.**
Har part scan hota hai. System check karta hai ki part is batch ke liye sahi hai ya nahi. Galat hai to scan turant BLOCKED hota hai.

**Problem 2: Kharab material ka asar pata nahi chalta.**
Agar ek material lot kharab nikle, to Forward Trace batata hai ki kaunse batches, shipments, customers aur vehicles affected hain.

Poori chain:

```text
Supplier → Material Lot → Production Batch → Serial → Shipment → Customer → VIN
```

Yeh chain tabhi banti hai jab har step ka data enter ho. Koi step chhoot gaya to trace mein wahi hissa khaali aayega.

---

## 2. Login aur roles

Demo accounts (real use se pehle password badalna zaroori hai):

| Role | Login | Password | Kaam |
|---|---|---|---|
| Admin | `admin` | `pass123` | Sab kuch, master data aur users |
| Operator | `operator1`, `operator2` | `pass123` | Scan, lot link, shipment mein batch add |
| Supervisor | `supervisor1` | `pass123` | Lot receive, batch start, serials, shipment, hold request |
| Quality Engineer | `priya` | `pass123` | Trace, inspection, hold request aur decision |
| Manager | `vikram` | `pass123` | Trace, hold decision, reports, audit log |
| User | Sign Up se khud banao | Apna | Sirf Dashboard aur Settings |

Sign Up karne wala hamesha `USER` banta hai. Role badalne ke liye Admin **Users** page par role change kare. Uske baad user ko sign out karke dobara login karna hoga.

**Password bhool gaye?**

1. Login page par **Forgot password?** dabao, email ya username daalo, phir **Ask admin for a code** dabao.
2. Admin **Users** page par us user ke saamne `RESET REQUESTED` dekhega aur **Reset password** dabayega. Ek code dikhega, jo sirf ek baar dikhta hai.
3. Admin ye code user ko khud bataye. Code 30 minute chalta hai aur sirf ek baar kaam karta hai.
4. User code aur naya password daale, phir naye password se login kare.

Har code aur reset **Audit Log** mein record hota hai. Isi tarah user ko bina request ke bhi code diya ja sakta hai. Agar kisi Admin ka password bhoola ho, to doosra Admin code banayega.

---

## 3. Data enter karne ka sahi order

Is order mein chalo, kyunki har step pichhle step ka data use karta hai.

| # | Screen | Kaun | Kyun |
|---|---|---|---|
| 1 | Parts | Admin | Finished part aur raw material define karne ke liye |
| 2 | BOM | Admin | Batata hai ki part kin components se banta hai |
| 3 | Suppliers, Machines, Customers | Admin | Baaki forms ki dropdown yahin se bharti hain |
| 4 | Material Lots | Supervisor | Supplier se aaya material record karne ke liye |
| 5 | Production Batches | Supervisor | Production shuru karne ke liye |
| 6 | Link lot | Operator | Batch aur lot ko jodne ke liye. Trace isi link par chalta hai |
| 7 | Generate serials | Supervisor | Har piece ko unique number dene ke liye |
| 8 | Scan Station | Operator | Part validate karne ke liye |
| 9 | Shipments | Supervisor, Operator | Batch kis customer ko gaya, yeh record karne ke liye |
| 10 | VIN Links | Admin | Batch kis vehicle mein laga, yeh record karne ke liye |
| 11 | Inspections, Holds | Quality Engineer, Manager | Quality problem record aur control karne ke liye |
| 12 | Forward, Backward Trace | Quality Engineer, Manager | Asar aur source dhundhne ke liye |

---

## 4. Har screen ki details

Neeche ke sample values sirf example hain. Number pehle se use ho chuka ho to naya number do.

### Parts

- **Kahan:** Administration → Parts
- **Kya enter karein:**

| Field | Example | Kyun |
|---|---|---|
| Part number | `FD-100` | Unique pehchan. Serial isi se banta hai |
| Name | `Front Door Handle` | Screens par dikhta hai |
| Cost per part | `25` | Loss estimate ke liye |
| Parts per vehicle | `4` | Vehicle estimate ke liye |
| Scrap cost | `5` | Loss estimate ke liye |
| Rework cost | `3` | Loss estimate ke liye |

- **Kya hoga:** part save hoga aur saari dropdowns mein aa jayega.
- **Kaise dhundhein:** Parts table mein.
- **Dhyan rakhein:** jo part kahin use ho chuka hai, wo delete nahi hoga. Kaggle wale parts mein scrap, rework aur parts per vehicle ki real values nahi hain, isliye demo se pehle ye bharo.

### BOM

- **Kahan:** Administration → BOM
- **Kya enter karein:** part chuno, phir Component aur Quantity. Example: `Front Door Handle` mein `Steel Sheet`, quantity `2`.
- **Kyun:** scan ke time system yahi list dekhta hai.
- **Kya hoga:** agar serial ka part batch ke part se alag hai aur BOM mein bhi nahi hai, to scan `WRONG_PART` se block hoga.

### Suppliers, Machines, Customers

- **Kahan:** Administration menu
- **Kya enter karein:** Code aur Name zaroori hain. Machines mein Station, Location aur In service bhi hain.
- **Kyun:** lot ko supplier chahiye, batch ko machine chahiye, shipment ko customer chahiye.
- **Use case:** nayi machine aayi to yahan add karo, tabhi wo batch form mein dikhegi. Jo machine In service nahi hai, wo dropdown mein nahi aayegi.

### Material Lots

- **Kahan:** Production → Material Lots
- **Kya enter karein:**

| Field | Example | Kyun |
|---|---|---|
| Lot number | `LOT-2026-101` | Is lot ko trace karne ki pehchan |
| Supplier | List se | Backward trace yahin tak pahunchta hai |
| Material | `Steel Sheet` | Kaunsa kachcha maal aaya. Zaroori hai, product mat chuno |
| Received | Aaj ki date | Kab aaya |
| Expiry | Optional | Is date ke baad lot naye batch mein nahi lagega |
| Quantity, Unit | `100`, `kg` | Kitna aaya. Quantity zaroori hai. Unit wahi rakho jismein Bill of materials likha hai |

- **Kya hoga:** lot `OK` status ke saath save hoga.
- **Kaise dhundhein:** Material Lots table. Status `OK`, `ON_HOLD` ya `REJECTED` dikhega. **Left** column batata hai ki kitna material bacha hai. **Can make** batata hai ki bache maal se kaunsa product kitna ban sakta hai. Expiry nikal chuki ho to `EXPIRED` dikhega.

### Production Batches

- **Kahan:** Production → Production Batches
- **Kya enter karein:** Batch number (`B-2026-101`), Part, Machine, Operator, Shift, Quantity (`10`).
- **Phir table mein batch ke saamne Open dabao aur:**
  1. **Link lot:** jo lot is batch mein use hua, use chuno. Poore batch ke liye kitna maal chahiye, wo **Material used** mein apne aap bhar jayega. Usi lot ko dobara link karne par matra jud jati hai.
  2. **Generate serials:** kitne serials chahiye likho, jaise `5`. Juda hua maal kam ho to app rok dega.
  3. **Show QR:** serial ka QR code dekho.
- **Kyun:** lot link ke bina forward trace ko batch nahi milega. Serial ke bina scan nahi hoga.
- **Dhyan rakhein:** lot ka material is product ke Bill of materials mein na ho to link nahi hota. `ON_HOLD`, `REJECTED` ya expired lot naye batch se link nahi hota. Lot mein jitna bacha hai, usse zyada nahi nikal sakte. Expiry ke din tak lot chalta hai. Jo batch pehle se link hai, wo lot expire hone par block nahi hota.

### Scan Station

- **Kahan:** Production → Scan Station
- **Kya enter karein:** Production batch, Station (`Assembly-A1`), Serial number. Camera ho to **Scan QR with camera** se bhi serial aa sakta hai. Bina camera ke **Demo QR** chunkar **Scan this QR** dabao.
- **Kaise kaam karta hai:** system yeh checks karta hai aur result save karta hai.

| Result | Matlab |
|---|---|
| `ACCEPTED` | Part sahi hai |
| `SERIAL_NOT_FOUND` | Yeh serial system mein nahi hai |
| `MANUAL_ENTRY_NOT_APPROVED` | Manual entry bina sahi supervisor password ke ki gayi |
| `LOT_ON_HOLD` | Batch ka koi lot hold par hai |
| `LOT_REJECTED` | Batch ka koi lot rejected hai |
| `WRONG_PART` | Part batch ya BOM ke hisaab se galat hai |
| `DUPLICATE_SERIAL` | Yeh serial is batch mein pehle accept ho chuka hai |

- **Manual entry:** QR na chale to Manual entry tick karo. Supervisor chuno aur supervisor apna password daale.
- **Kaise dhundhein:** Dashboard ke Recent scans mein.

### Shipments

- **Kahan:** Production → Shipments
- **Kya enter karein:** Shipment number (`SH-2026-101`), Customer, Shipped date. Phir **Add a batch to a shipment** mein shipment, batch aur quantity.
- **Kyun:** isse batch customer se judta hai aur trace mein shipped dikhta hai.
- **Dhyan rakhein:** jis batch ka koi lot `ON_HOLD` ya `REJECTED` hai, wo shipment mein add nahi hoga.

### VIN Links

- **Kahan:** Administration → VIN Links
- **Live link:** VIN search karo, Vehicle chuno, Batch chuno, Link vehicle dabao.
- **CSV import:** columns `vin,batch_number,serial,customer_code`. Pehli line header hogi. Preview dabane par data save nahi hota, Import dabane par hota hai.
- **Kyun:** isse batch vehicle se judta hai.
- **Kaise dhundhein:** Linked vehicles table mein.

### Inspections

- **Kahan:** Trace & Quality → Inspections
- **Kya enter karein:** lot, Result (`PASS` ya `FAIL`), Notes (`Tensile strength below limit`).
- **Kyun:** quality check ka record rehta hai.
- **Dhyan rakhein:** `FAIL` likhne se hold apne aap nahi banta. Hold alag se request karna hoga.

### Hold Management

- **Kahan:** Trace & Quality → Hold Management
- **Step 1:** Quality Engineer ya Supervisor lot chunkar reason likhe (kam se kam 10 characters).
- **Step 2:** koi **dusra** Quality Engineer, Manager ya Admin Approve ya Reject kare.
- **Approve par:** lot `ON_HOLD` ho jata hai aur us lot wale batches ke scans block ho jate hain.
- **Reject par:** lot status nahi badalta.
- **Release:** agar lot baad mein theek nikle, to Manager ya Admin Hold Management par **Lots on hold** mein reason likh kar lot release kare. Lot wapas `OK` ho jayega aur ye Audit Log mein dikhega.
- **Reject lot:** agar lot retest mein bhi fail ho, to Manager ya Admin **Lots on hold** mein reason likh kar **Reject lot** dabaye. Lot `REJECTED` ho jayega. Ye final hai, wapas `OK` nahi hota, aur Audit Log mein dikhta hai. Sirf hold par pada lot reject hota hai, taaki do-person hold ka rule bana rahe.
- **Dhyan rakhein:** jisne request ki, wo khud decide nahi kar sakta.

### Forward Trace

- **Kahan:** Trace & Quality → Forward Trace
- **Kab use karein:** jab lot kharab nikle.
- **Kya karein:** lot chuno, Run trace dabao.
- **Kya milega:** affected batches, shipped ya factory mein, shipments, customers, VINs, estimated vehicles, loss aur trace ka time.
- **Formula:** Loss = quantity × (cost + scrap + rework). Vehicles = quantity ÷ parts per vehicle. Amounts USD mein dikhte hain.

### Backward Trace

- **Kahan:** Trace & Quality → Backward Trace
- **Kab use karein:** jab customer kisi vehicle ki problem bataye.
- **Kya karein:** VIN ya serial daalo, Trace backward dabao.
- **Kya milega:** VIN, customer, serial, batch, part, material lots aur suppliers.

### Dashboard, Reports, Audit Log, Settings

| Screen | Kya dikhata hai | Kaun dekh sakta hai |
|---|---|---|
| Dashboard | Aaj ke scans, blocked, lots on hold, pending holds, 7 din ki activity | Sab |
| Reports | Block reasons, lot status, hold decisions, inspections, shipments | Admin, Manager |
| Audit Log | Kisne kya kiya aur kab | Admin, Manager |
| Settings | Naam aur password badalna | Sab |

---

## 5. Common problems

| Problem | Kya karein |
|---|---|
| Server could not be reached | Backend aur database chal rahe hain, check karo |
| Your role does not have access | Sahi role se login karo |
| Dropdown khaali hai | Pehle us master data ko add karo |
| Record already exists or is still in use | Number duplicate hai, ya record kahin use ho raha hai |
| Trace mein batch nahi dikha | Batch se lot link karo |
| Trace mein VIN nahi dikhi | VIN Links par vehicle link karo |
| Naya role nahi dikha | Sign out karke dobara login karo |
| Purana page dikh raha hai | Ctrl+Shift+R dabao |
| Material lot expired on ... | Expired lot naya batch nahi le sakta. Dusra lot use karo |
| Material lot has only ... left | Lot mein itna material nahi bacha. Kam matra likho ya dusra lot lo |
| Batch has room for only ... more serials | Batch ki quantity se zyada serials nahi ban sakte |
| Batch has only ... pieces left to ship | Batch ki quantity se zyada ship nahi ho sakta |
| Hold request is already waiting | Pehle wali request par decision lo |
| This is the last active admin | Pehle doosra admin banao ya enable karo |

---

## 6. Data kahan milega

| Data | Table |
|---|---|
| Users | `users` |
| Parts, BOM | `parts`, `bom_items` |
| Lots, batches, link | `material_lots`, `production_batches`, `batch_material_consumption` |
| Serials, scans | `serial_numbers`, `scan_events` |
| Shipments | `shipments`, `shipment_items` |
| VIN | `vehicle_links` |
| Quality | `quality_inspections`, `quality_holds` |
| Trace aur audit | `trace_runs`, `audit_logs` |

Tables Flyway migrations se bante hain: `backend/src/main/resources/db/migration`.
