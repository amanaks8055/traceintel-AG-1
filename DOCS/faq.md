# TraceIntel: Questions and Answers

Use this page when the app refuses something or you do not understand a message. For the full steps, see `how_to_use.md`.

## Login and password

**How do I log in?**
Use your username or your email. The email works in capital letters too.

**I forgot my password. Where do I get the reset code?**
Only an Admin can give you the code. It does not come by email.
1. On the login page, click **Forgot password?**, then click **Ask admin for a code**.
2. The Admin opens the **Users** page and clicks **Reset password**. The code appears there.
3. The Admin gives you the code.
4. Enter the code and your new password.

**How long does the code work?**
For 30 minutes, and only once. If it expires, ask the Admin for a new one.

**What if an Admin forgets their password?**
Another Admin can create a code for them. This is why you should keep at least two admins.

**What if both admins forget their passwords?**
There is no way to fix this inside the app. Someone with access to that PC and the database must fix the password in the database.

**Why can't I disable the last admin?**
Nobody would be able to manage users, and the system would be locked. Create another admin first.

**Why do I only see Dashboard and Settings after signing up?**
A new sign up always gets the `USER` role. Ask the Admin to give you a role on the **Users** page. Then sign out and log in again.

**How long must a password be?**
At least 8 characters. The demo password `pass123` is older and still works, but change it before real use.

## Material lots

**Why is lot quantity required?**
The app uses it to track how much material is left. The **Left** column on the Material Lots page shows this.

**What does "Material lot has only ... left" mean?**
The lot does not have as much material as you entered. Enter a smaller amount or use another lot.

**I entered the wrong quantity. Can I fix it?**
Not at the moment. You cannot reduce Material used on screen, because the amount only adds up. Check the amount before you link the lot.

**What does the "Can make" column mean?**
It shows how many pieces of each product the material left in the lot is enough for. The app uses the **Bill of materials**. If one Door Handle needs 2 kg of steel and 100 kg is left, it shows 50.

**Why is "Can make" empty?**
Either the lot is not `OK`, or no product in the Bill of materials uses this material. An Admin can add it on the **Bill of materials** page.

**What does "... is not in the bill of materials for ..." mean?**
You chose a lot with the wrong material for this product. The app stops it so the wrong product is not made. Choose a lot with the right material. If the material is right, ask an Admin to add it to the Bill of materials.

**Why are some parts under "Raw materials" on the Bill of materials page?**
Parts whose number starts with `RAW-` are shown there first. Give a new raw material a `RAW-` number on the Parts page and it will appear in that group.

**What does "... has no material" mean?**
This lot was added without a material, so the app cannot check it. Add the lot again with its material.

**What does "Material lot expired on ..." mean?**
The expiry date has passed, so the lot cannot be used in a new batch. A lot can still be used on its expiry date.

**A lot expired. What happens to the batches that already use it?**
They carry on as normal. The lot was fine when the material was used.

**Why won't my expiry date save?**
The expiry date cannot be before the received date.

## Batches and serials

**What does "Batch has room for only ... more serials" mean?**
A batch can only have as many serials as its quantity. If the quantity is 10, you cannot make more than 10.

**Why must I link a lot to a batch?**
Tracing depends on this link. Without it, Forward Trace cannot find the batch. The app also will not make serials until a lot is linked.

**What does "... pieces need ... but only ... is linked" mean?**
There is not enough material linked to the batch for this many pieces. Link more material from a lot, or make fewer serials.

**Does the app check every material in the Bill of materials?**
No. It checks only the materials you have linked to the batch. If a product needs steel and screws and you link only steel, the app does not check the screws.

## Scanning

**Why was my scan BLOCKED?**

| Reason | What it means | What to do |
|---|---|---|
| `SERIAL_NOT_FOUND` | This serial is not in the system | Check the serial |
| `WRONG_PART` | This part does not belong to this batch | Bring the correct part |
| `DUPLICATE_SERIAL` | This serial was already scanned in this batch | Scan a different part |
| `LOT_ON_HOLD` | A lot used in this batch is on hold | Wait for the quality decision |
| `LOT_REJECTED` | A lot used in this batch was rejected | This batch cannot move forward |
| `MANUAL_ENTRY_NOT_APPROVED` | The manual entry was not approved by a supervisor | Ask a supervisor to approve it |

**Are blocked scans saved?**
Yes. Accepted and blocked scans are both recorded and shown on the Dashboard.

## Shipments

**Why can't I add a batch to a shipment?**
There are two possible reasons:
- A lot used in the batch is `ON_HOLD` or `REJECTED`.
- You are shipping more than the batch quantity. The message tells you how many pieces are left.

**What if a lot goes on hold after its batch has shipped?**
The shipment cannot be stopped, because it has already left. Run **Forward Trace** to see which customers and vehicles are affected.

## Hold, Release and Reject

**Why does a hold need two people?**
So that one wrong decision does not stop production. One person requests the hold and a different person decides. The person who requested it cannot approve it.

**What does "Hold request is already waiting" mean?**
This lot already has a pending request. Decide that request first.

**What is the difference between rejecting a hold request and rejecting a lot?**

| | Reject a hold request | Reject a lot |
|---|---|---|
| Meaning | The hold is not needed | The material is bad |
| Lot status | Stays the same | Becomes `REJECTED` |
| Can it be undone? | A new request can be made | No, it is final |

**When should I release or reject a lot?**
You can only do these on a lot that is on hold.
- **Release:** the retest passed. The lot goes back to `OK`.
- **Reject:** the retest failed. The lot stays `REJECTED` for good.

A Manager or Admin does both. A reason is required, and both appear in the Audit Log.

**Why can't I reject an `OK` lot directly?**
That would break the two-person hold rule. Place a hold first, then reject the lot.

## Trace

**What is the difference between Forward Trace and Backward Trace?**
- **Forward:** a lot turned out bad. It shows where that lot went.
- **Backward:** a vehicle has a problem. It shows which lot and supplier the material came from.

**Why is a batch or VIN missing from the trace?**
For a missing batch, link the lot to the batch. For a missing VIN, link the vehicle to the batch on **VIN Links**.

## Data and setup

**Why is the app empty when it opens?**
Lots, batches, scans and shipments are entered live in front of managers. Suppliers, parts, customers and VINs are already loaded. They come from Kaggle data and some demo records. Some names look like real companies, but this is not real company data.

**What does "The server could not be reached" mean?**
The backend or database is not running. This often happens after a PC restart. Run `start-traceintel.cmd`.

**What does "Your role does not have access" mean?**
Your role is not allowed to do this. Log in with the correct role.

**Why is a dropdown empty?**
The data it needs has not been added yet. For example, a batch needs a part and a machine.

**Why am I seeing an old page?**
Press Ctrl+Shift+R.

## Current limits

**What can the app not do yet?**
- Reset codes are not sent by email.
- There is no limit on login attempts.
- After a password reset, an old login session can keep working for up to 24 hours.
- Demo passwords must be changed before real use.
