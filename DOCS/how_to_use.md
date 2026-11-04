# TraceIntel Kaise Use Karein

Ye guide shuru se ant tak ek hi kahani chalati hai. Har step mein likha hai ki kaun login kare, kahan jaye, kya bhare, kya result aayega aur uska matlab kya hai.

Kisi error ka matlab dekhna ho to `faq.md` kholo. Har screen ki details `user_guide.md` mein hain.

## Kahani kya hai

Factory mein steel aata hai. Usse Door Handle bante hain, aur wo Maruti ko jaate hain. Baad mein pata chalta hai ki steel kharab tha. Ab app batayegi ki kitne parts, kaunsa customer aur kaunsi gaadi affected hai.

```text
Steel aaya → Batch bana → Parts scan hue → Maruti ko gaye → Gaadi se jude → Steel kharab nikla → Trace
```

## Shuru karne se pehle

1. `start-traceintel.cmd` chalao. PC restart ke baad ye zaroori hai.
2. Browser mein `http://localhost:5173` kholo.
3. Upar **Sign in** dabao.

Har step mein alag login lagega, kyunki factory mein har kaam alag log karte hain. Login badalne ke liye left menu mein neeche **Sign out** dabao.

| Login | Password | Role | Is kahani mein kaam |
|---|---|---|---|
| `supervisor1` | `pass123` | Supervisor | Material aur batch |
| `operator1` | `pass123` | Operator | Scan |
| `admin` | `pass123` | Admin | Gaadi jodna |
| `priya` | `pass123` | Quality Engineer | Quality check |
| `vikram` | `pass123` | Manager | Decision aur trace |

> Ye kahani pehli baar ke hisaab se likhi hai. Dobara chalani ho to number badal do, jaise `LOT-2026-002`. Serial number bhi aage badh jayega, isliye wo batch ki Serial list se dekh lena.

---

## Part 1: Material aur production

### Step 1: Material receive karo

**Login:** `supervisor1`
**Kahan:** Material Lots

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Lot number | `LOT-2026-001` | Is delivery ki pehchan |
| Supplier | `Tata Steel Ltd (SUP-TSL)` | Material kisne bheja |
| Material | `RAW-STL-001 - Steel Sheet` | Kya aaya |
| Received | Aaj ki date | Kab aaya |
| Expiry | Khaali chhod do | Optional. Iske baad lot naye batch mein nahi lagega |
| Quantity | `100` | Kitna aaya |
| Unit | `kg` | Kis unit mein |

**Dabao:** Add lot

**Result:** `Lot saved.` dikhega. Table mein Quantity `100 kg`, Left `100 kg`, Can make `Door Handle Assembly: 50`, Status `OK` aayega.

**Matlab:** ab system jaanta hai ki 100 kg steel aaya hai aur poora bacha hua hai. Ek Door Handle mein 2 kg steel lagta hai (ye **Bill of materials** mein likha hai), isliye 100 kg se 50 ban sakte hain. Ye hisaab app khud lagata hai.

> **Material** mein hamesha kachcha maal chuno, jaise Steel Sheet. Product mat chuno, jaise Bumper. Galat material wala lot kisi batch mein nahi judega.

### Step 2: Batch shuru karo

**Login:** `supervisor1`
**Kahan:** Production Batches

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Batch number | `B-2026-001` | Is production run ki pehchan |
| Part | `MTH-DH-001 - Door Handle Assembly` | Kya ban raha hai |
| Machine | `ASM-001 - Assembly Station 1` | Kis machine par |
| Operator | `Rajesh Kumar` | Kisne banaya |
| Shift | `DAY` | Kis shift mein |
| Quantity | `10` | Kitne pieces banenge |

**Dabao:** Start batch

**Result:** All batches table mein `B-2026-001` aa jayega.

**Matlab:** production record shuru ho gaya. Abhi isme material aur serial nahi hain.

### Step 3: Batch se material jodo

Table mein `B-2026-001` ke saamne **Open** dabao.

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Material lot used | `LOT-2026-001 - Steel Sheet (OK, 100 kg left)` | Kaunsa material laga |
| Material used | `20` apne aap bhar jayega | Kitna laga |

Lot chunte hi neeche hisaab dikhega: `1 piece needs 2 kg. This batch of 10 needs 20 kg. 100 kg is left in the lot, enough for 50 pieces.`

**Dabao:** Link lot

**Result:** batch ke Lots column mein `LOT-2026-001` dikhega. Material Lots page par Left ab `80 kg` aur Can make `40` hoga.

**Matlab:** ye sabse zaroori link hai. Isi se aage trace chalega. Iske bina app ko pata nahi chalega ki steel kis batch mein gaya.

> Galat maal chuna, jaise Door Handle ke batch mein rubber, to laal message aayega aur **Link lot** button band ho jayega. App galat product banne nahi deta.

### Step 4: Serial number banao

Usi Open wale box mein:

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Serials to generate | `5` | Kitne pieces ko number dena hai |

**Dabao:** Generate serials

**Result:** `MTH-DH-001-00001` se `MTH-DH-001-00005` tak list aayegi. **Show QR** dabane par QR code dikhega.

**Matlab:** har piece ko apni pehchan mil gayi. Batch ki quantity 10 hai, isliye 10 se zyada serials nahi banenge.

> Serial tabhi banenge jab batch mein lot juda ho aur maal poora ho. 5 pieces ko 10 kg steel chahiye aur 20 kg juda hai, isliye ye chal gaya. Maal kam hota to app rok deta, jaise: `12 pieces need 24 kg of Steel Sheet, but only 20 kg is linked`.

### Step 5: Dusra batch banao

Ye sirf isliye hai taaki Step 6 mein galat part dikha sako. Is product ke liye alag maal chahiye, isliye pehle **Material Lots** par Step 1 ki tarah ek aur lot add karo:

| Field | Kya bharna hai |
|---|---|
| Lot number | `LOT-CU-001` |
| Supplier | Koi bhi |
| Material | `RAW-COP-001 - Copper Wire Bundle` |
| Received | Aaj ki date |
| Quantity | `50` |
| Unit | `kg` |

Phir **Production Batches** par Step 2 ki tarah bharo:

| Field | Kya bharna hai |
|---|---|
| Batch number | `B-2026-002` |
| Part | `MTH-WH-002 - Wiring Harness Module` |
| Machine | `ASM-001 - Assembly Station 1` |
| Operator | `Rajesh Kumar` |
| Shift | `DAY` |
| Quantity | `5` |

**Dabao:** Start batch, phir Open. Material lot used mein `LOT-CU-001` chuno. Material used mein `15` apne aap aayega. **Link lot** dabao. Phir Serials to generate mein `2` likho aur **Generate serials** dabao.

**Result:** `MTH-WH-002-00001` aur `MTH-WH-002-00002` banenge.

---

## Part 2: Scan

### Step 6: Parts scan karo

**Login:** `operator1`
**Kahan:** Scan Station

Pehle ye bharo. Ye charo scans mein same rahega:

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Production batch | `B-2026-001 - Door Handle Assembly` | Abhi line par kaunsa batch chal raha hai |
| Station | `Assembly-A1` | Scan kahan hua |

Ab **Serial number** mein ye ek-ek karke daalo aur har baar **Validate scan** dabao.

| # | Serial number | Result | Reason | Matlab |
|---|---|---|---|---|
| 1 | `MTH-DH-001-00001` | `ACCEPTED` | - | Sahi part hai |
| 2 | `MTH-DH-001-00001` | `BLOCKED` | `DUPLICATE_SERIAL` | Ye piece pehle scan ho chuka hai |
| 3 | `MTH-WH-002-00001` | `BLOCKED` | `WRONG_PART` | Door Handle ke batch mein Wiring Harness aa gaya |
| 4 | `ABC-999` | `BLOCKED` | `SERIAL_NOT_FOUND` | Ye number system mein hai hi nahi |

**Matlab:** galat part line par hi ruk gaya. Yahi is app ka pehla bada fayda hai. Blocked scans bhi record hote hain.

> **Bina camera ke QR test:** batch chunte hi us batch ka **Demo QR** dikhega. Serial chuno aur **Scan this QR** dabao. Serial number apne aap bhar jayega, phir **Validate scan** dabao.
>
> Camera ho to **Scan QR with camera** se serial apne aap aa jata hai. Laptop camera apni hi screen nahi dekh sakta, isliye QR phone par dikhao ya print karo. QR kharab ho to **Manual entry** tick karo. Tab supervisor ka password lagega.

---

## Part 3: Dispatch

### Step 7: Customer ko bhejo

**Login:** `supervisor1`
**Kahan:** Shipments

**Create a shipment** box mein:

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Shipment number | `SH-2026-001` | Is dispatch ki pehchan |
| Customer | `Maruti Suzuki India Ltd` | Maal kisko gaya |
| Shipped date | Aaj ki date | Kab gaya |
| Notes | Khaali chhod do | Optional |

**Dabao:** Create shipment

Phir **Add a batch to a shipment** box mein:

| Field | Kya bharna hai |
|---|---|
| Shipment | `SH-2026-001` |
| Batch | `B-2026-001 - Door Handle Assembly` |
| Quantity | `10` |

**Dabao:** Add batch

**Result:** All shipments table mein Batches ke neeche `B-2026-001 (10)` dikhega.

**Matlab:** ab pata hai ki ye batch Maruti ko gaya hai.

### Step 8: Gaadi se jodo

**Login:** `admin`
**Kahan:** VIN Links

VIN gaadi ka unique number hota hai.

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Vehicle | List ki koi bhi VIN | Part kis gaadi mein laga |
| Batch | `B-2026-001 - Door Handle Assembly` | Kaunsa batch |
| Customer (optional) | `Maruti Suzuki India Ltd` | Isse gaadi Maruti ki dikhegi |

**Dabao:** Link vehicle

**Result:** Linked vehicles table mein ye VIN aa jayegi. **Ye VIN copy kar lo**, Step 14 mein chahiye.

**Matlab:** chain poori ho gayi. Supplier se gaadi tak sab juda hua hai.

---

## Part 4: Quality problem

### Step 9: Inspection likho

**Login:** `priya`
**Kahan:** Inspections

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Material lot | `LOT-2026-001` | Kis lot ki jaanch hui |
| Result | `FAIL` | Jaanch ka nateeja |
| Notes | `Hardness below limit` | Kya galat mila |

**Dabao:** Save inspection

**Result:** Inspection history mein `FAIL` dikhega.

**Matlab:** sirf record bana hai. Lot abhi bhi `OK` hai, kyunki hold alag se lagta hai.

### Step 10: Hold ki request karo

**Login:** `priya`
**Kahan:** Hold Management

| Field | Kya bharna hai | Kyun |
|---|---|---|
| Material lot | `LOT-2026-001 (OK)` | Kis lot ko rokna hai |
| Reason | `Inspection failed, hardness below limit` | Kam se kam 10 characters |

**Dabao:** Request hold

**Result:** Quality holds table mein status `REQUESTED` aayega.

**Ab ye karke dekho:** usi row mein **Approve** dabao. Error aayega: `Requester and approver cannot be the same person`.

**Matlab:** ek insaan akele hold nahi laga sakta. Isliye ek doosra aadmi decide karega.

### Step 11: Manager hold approve kare

**Login:** `vikram`
**Kahan:** Hold Management

| Field | Kya bharna hai |
|---|---|
| Notes | `Agreed, stop this lot` |

**Dabao:** usi row mein Approve

**Result:** hold ka status `APPROVED` aur lot ka status `ON_HOLD` ho jayega.

**Matlab:** ye lot ab ruk gaya hai. Is lot wale batches na scan honge, na ship honge, aur ye lot naye batch mein nahi lagega.

### Step 12: Scan rukta hai, dekho

**Login:** `operator1`
**Kahan:** Scan Station

| Field | Kya bharna hai |
|---|---|
| Production batch | `B-2026-001 - Door Handle Assembly` |
| Serial number | `MTH-DH-001-00002` |

**Dabao:** Validate scan

**Result:** `BLOCKED`, message `Material lot is ON HOLD`, reason `LOT_ON_HOLD`.

**Matlab:** serial sahi hai, par material kharab hai, isliye part aage nahi jayega.

---

## Part 5: Trace

### Step 13: Forward Trace, yaani asar kitna hai

**Login:** `vikram`
**Kahan:** Forward Trace

| Field | Kya bharna hai |
|---|---|
| Material lot | `LOT-2026-001 (ON_HOLD)` |

**Dabao:** Run trace

**Result aur matlab:**

| Screen par | Value | Matlab |
|---|---|---|
| Batch, Location | `B-2026-001`, `SHIPPED` | Is lot ka batch factory se ja chuka hai |
| Shipment, Customer | `SH-2026-001`, Maruti | Kisko call karna hai |
| Affected VINs | `1` | Kitni linked gaadiyan affected hain |
| Affected parts | `10` | Batch ki quantity |
| Estimated vehicles | `2` | 10 ÷ 4 parts per vehicle |
| Parts cost | `USD 3,000` | 10 × 300 |
| Scrap cost | `USD 800` | 10 × 80 |
| Rework cost | `USD 450` | 10 × 45 |
| Estimated loss | `USD 4,250` | Teeno ka total |

**Matlab:** ek click mein poora asar pata chal gaya. Upar likha time batata hai ki trace kitne milliseconds mein hua. Ye har run mein thoda alag hoga. Estimates andaza hain, pakka nuksaan nahi.

### Step 14: Backward Trace, yaani problem kahan se aayi

**Login:** `vikram`
**Kahan:** Backward Trace

| Field | Kya bharna hai |
|---|---|
| VIN or serial | Step 8 ki VIN |

**Dabao:** Trace backward

**Result:** VIN, Customer `Maruti Suzuki India Ltd`, Batch `B-2026-001`, Part `Door Handle Assembly`, aur Material lot `LOT-2026-001, Tata Steel Ltd`.

**Matlab:** customer ne gaadi ki shikayat ki, aur app ne supplier tak ka raasta dikha diya. VIN ki jagah `MTH-DH-001-00001` daalne par bhi batch, lot aur supplier mil jate hain.

---

## Part 6: Faisla

### Step 15: Lot ka kya karna hai

**Login:** `vikram`
**Kahan:** Hold Management → Lots on hold

| Field | Kya bharna hai | Kyun |
|---|---|---|
| LOT-2026-001: reason | `Retest passed, material is fine` | Faisle ka record |

Ab dono mein se ek dabao:

| Button | Kab dabana hai | Result |
|---|---|---|
| **Release lot** | Retest pass hua | Lot wapas `OK` |
| **Reject lot** | Retest fail hua | Lot `REJECTED`. Ye final hai, wapas nahi hota |

Is kahani ke liye **Release lot** dabao.

**Result:** `No lots are on hold.` dikhega.

**Ab ye karke dekho:** `operator1` se `MTH-DH-001-00002` dobara scan karo. Ab result `ACCEPTED` aayega.

### Step 16: Poora record dekho

**Login:** `vikram`

| Kahan | Kya dikhega | Matlab |
|---|---|---|
| **Dashboard** | Production batches `2`, Scans today `6`, Blocked today `4`, Lots on hold `0` | Plant ka aaj ka haal |
| **Dashboard → Why scans were blocked** | Duplicate, Serial not found, Lot on hold, Wrong part, sab `1` | Scan kyun ruke |
| **Reports** | Lot status, Hold decisions, Inspection results, Shipments by customer | Poora summary |
| **Audit Log** | Kisne kya kiya aur kab, jaise `LOT_RELEASED` | Har kaam ka record |

**Kahani poori ho gayi.** Ab aap app ka pura flow jaante ho.

---

## Baaki kaam kab karne hain

| Kaam | Kaun | Kahan |
|---|---|---|
| Naya part, supplier, machine ya customer | `admin` | Administration menu |
| Part ke components | `admin` | BOM |
| Naya user banana ya role dena | `admin` | Users |
| Password bhool gaye | Koi bhi | Login page → Forgot password? |
| Apna naam ya password badalna | Koi bhi | Settings |

> Yahan ke parts, suppliers aur customers demo records hain. Inme kuch naam asli companies jaise hain, par ye real company data nahi hai.
