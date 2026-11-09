"""
Maps the old Firebase RTDB export (silk = Textiles, coolie = Factory) onto the new app's seed data.

  python -I import_rtdb_export.py "<export>.json"

Writes (relative to this file):
  composeApp/src/commonMain/kotlin/com/elvan/udukkai/data/mock/MockData.kt      the seed data (read by both dev seeders)
  composeApp/src/commonMain/composeResources/files/seed/*.txt                   the company images ("seed:<name>" in MockData)
and prints the counts per table, to check against the export. See RTDB_IMPORT_NOTES.md for what is mapped.
"""
import json, math, os, re, sys
from collections import Counter

SRC = sys.argv[1]
HERE = os.path.dirname(os.path.abspath(__file__))
APP = os.path.join(HERE, "..", "composeApp", "src", "commonMain")
D = json.load(open(SRC, encoding="utf8"))


# ---------- Kotlin literal helpers ----------
def s(v):
    return '"' + str(v).replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$").replace("\n", "\\n") + '"'

def num(v):
    f = float(v)
    return repr(int(f)) + ".0" if f == int(f) else repr(f)

def bi(ta, en):
    ta, en = (ta or ""), (en or "")
    return "bi(%s, %s)" % (s(ta), s(en))

def kmap(d):
    return "mapOf(" + ", ".join("%s to %s" % (s(k), s(v)) for k, v in d.items()) + ")"

def f(v):
    try:
        return float(v)
    except (TypeError, ValueError):
        return 0.0

def iso(d):  # "21/02/2026" or "2026-06-11" -> "2026-06-11"
    if "/" in d:
        dd, mm, yy = d.split("/")
        return "%s-%s-%s" % (yy, mm.zfill(2), dd.zfill(2))
    return d

def first(*vals):
    for v in vals:
        if v not in (None, ""):
            return v
    return ""


# ---------- customer ----------
def customer_fields(c):
    """Flat snapshot keys the app prints with a bill (CustomerSnapshot): name.ta, city.en, pinCode ..."""
    out = {}
    def put(k, ta, en):
        if ta: out[k + ".ta"] = ta
        if en: out[k + ".en"] = en
    put("name", first(c.get("name_Tamil"), c.get("name")), first(c.get("name_English"), c.get("nameEn")))
    put("address", first(c.get("mugavari_Tamil"), c.get("mugavari"), c.get("address1_Tamil"), c.get("address1"), c.get("address")),
        first(c.get("mugavari_English"), c.get("mugavariEn"), c.get("address1_English"), c.get("address1En"), c.get("addressEn")))
    put("city", first(c.get("oor_Tamil"), c.get("oor"), c.get("city_Tamil"), c.get("city")),
        first(c.get("oor_English"), c.get("oorEn"), c.get("city_English"), c.get("cityEn")))
    put("district", first(c.get("maavattam_Tamil"), c.get("maavattam")), first(c.get("maavattam_English"), c.get("maavattamEn")))
    put("state", first(c.get("maanilam_Tamil"), c.get("maanilam")), first(c.get("maanilam_English"), c.get("maanilamEn")))
    put("country", first(c.get("country_Tamil"), c.get("country")), first(c.get("country_English"), c.get("countryEn")))
    for k, src in (("pinCode", "pin"), ("gstin", "gstin"), ("email", "email"), ("phone", "tholaipesi")):
        v = c.get(src) or (c.get("phone") if k == "phone" else "")
        if v: out[k] = v
    return out

def customer_kt(ref, c):
    g = customer_fields(c)
    return "SeedCustomer(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)" % (
        s(ref), bi(g.get("name.ta"), g.get("name.en")), bi(g.get("address.ta"), g.get("address.en")),
        bi(g.get("city.ta"), g.get("city.en")), bi(g.get("district.ta"), g.get("district.en")),
        bi(g.get("state.ta"), g.get("state.en")), bi(g.get("country.ta"), g.get("country.en")),
        s(g.get("pinCode", "")), s(g.get("gstin", "")), s(g.get("email", "")), s(g.get("phone", "")))


# ---------- images ----------
SEED = os.path.join(APP, "composeResources", "files", "seed")
def image(name, data_url):
    if not data_url:
        return ""
    path = os.path.join(SEED, name + ".txt")
    if not os.path.exists(path) or open(path, encoding="utf8").read() != data_url:
        open(path, "w", encoding="utf8", newline="").write(data_url)
    return "seed:" + name


# ---------- profiles ----------
def profile_kt(short, name_ta, name_en, phone, email, gstin, address, city, district, state, country, pin,
               bank, branch, acct, ifsc, logo, wide, layout, sig_img, sig_ta, sig_en, tagline, theme, print_settings, pan=""):
    p = [x.strip() for x in phone.split(",") if x.strip()]
    p += [""] * (2 - len(p))
    return """    SeedProfile(
        shortName = %s, businessName = %s,
        phone1 = %s, phone2 = %s, email = %s, gstin = %s, pan = %s,
        address = %s, city = %s, district = %s, state = %s, country = %s, pinCode = %s,
        bankName = %s, branch = %s, accountNumber = %s, ifsc = %s,
        logo = %s, wideLogo = %s, headerLayout = %s, signature = %s, signatoryName = %s,
        tagline = %s, themeColor = %s,
        printSettings = %s
    )""" % (s(short), bi(name_ta, name_en), s(p[0]), s(p[1]), s(email), s(gstin), s(pan),
            bi(*address), bi(*city), bi(*district), bi(*state), bi(*country), s(pin),
            bi(*bank), bi(*branch), s(acct), s(ifsc), s(logo), s(wide), s(layout), s(sig_img), s(sig_ta),
            bi(*tagline), s(theme), kt_any(print_settings))

def kt_any(v):
    if isinstance(v, dict):
        return "mapOf(" + ", ".join("%s to %s" % (s(k), kt_any(x)) for k, x in v.items()) + ")"
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, (int, float)):
        return num(v)
    return s(v)


# ================= TEXTILES (silk) =================
P = D["profile"]
tx_print = {
    "logoHeight": P["logoHeight"], "wideLogoX": P["wideLogoX"], "wideLogoY": P["wideLogoY"], "wideLogoScale": P["wideLogoScale"],
    "invoiceDisplayOptions": dict(D["meta"]["invoiceDisplayOptions"]["value"]),
    "signatoryNames": {"ta": P["authorizedSignatoryName"], "en": P["signatureName"]},
}
tx_profile = profile_kt(
    P["shortBusinessName"], P["niruvanathinPeyar_Tamil"], P["niruvanathinPeyar_English"], P["phone"], P["email"], P["gstin"],
    (P["mugavari_Tamil"], P["mugavari_English"]), (P["oor_Tamil"], P["oor_English"]),
    (P["maavattam_Tamil"], P["maavattam_English"]), (P["maanilam_Tamil"], P["maanilam_English"]),
    (P["country_Tamil"], P["country_English"]), P["pin"],
    (P["vangiPeyar"], P["vangiPeyarEn"]), (P["bankBranch"], P["bankBranchEn"]), P["kanakkuEn"], P["ifsc"],
    image("sjps_logo", P["logo"]), image("sjps_wide_logo", P["wideLogo"]), P["billHeaderStyle"],
    image("sjps_signature", P["signature"]), P["authorizedSignatoryName"], P["signatureName"] and "",
    (P["tagline_Tamil"], P["tagline_English"]), "#388e3c", tx_print, P["pan"])
TX_SHORT = P["shortBusinessName"]

tx_items = D["porulgal"]
tx_item_kt = []
for k, it in tx_items.items():
    tx_item_kt.append("    SeedItem(%s, %s, %s, %s, %s, %s, %s)" % (
        s(k), bi(first(it.get("name_Tamil"), it.get("name")), first(it.get("name_English"), it.get("nameEn"))),
        s(it.get("hsn", "")), num(it.get("rate", 0)), num(it.get("taxPercent", 0)),
        s(it.get("measureType", "quantity")), s(it.get("unit", "Nos"))))

tx_customers = D["vanigargal"]
(tx_cust_ref, tx_cust), = tx_customers.items()

tx_inv_kt, tx_inv_seen, tx_inv_skipped = [], {}, []
for key, inv in D["pattiyalkal"].items():
    dd = inv["data"]
    no = inv["invoiceNumber"]
    sig = json.dumps([dd["items"], dd["details"]["invoiceDate"], inv["totalAmount"]], sort_keys=True)
    if no in tx_inv_seen:
        assert tx_inv_seen[no] == sig, "duplicate number with different content: " + no
        tx_inv_skipped.append((key, no))
        continue
    tx_inv_seen[no] = sig
    assert dd["client"]["gstin"] == tx_cust["gstin"] and dd["client"]["name_Tamil"] == tx_cust["name_Tamil"]
    lines = []
    for i in dd["items"]:
        pid = i.get("productId")
        if pid is not None and pid not in tx_items: pid = None
        raw = f(i.get("rawDiscountValue"))
        if i["discountType"] == "percentage":
            disc, dtype = raw, "%"
        else:
            assert abs(f(i["discount"]) - raw) < 1e-9, (no, i)
            disc, dtype = f(i["discount"]), "₹"
        lines.append("SeedLine(%s, %s, %s, %s, %s, %s, %s, %s, %s)" % (
            s(pid) if pid else "null", bi(i["name_Tamil"], i["name_English"]), s(i.get("hsn", "")), num(i["qty"]),
            s(i["unit"]), num(i["rate"]), num(i["taxPercent"]), num(disc), s(dtype)))
    t, o = dd["totals"], dd["invoiceOptions"]
    gtype = "%" if t["globalDiscountType"] == "percentage" else "₹"
    tx_inv_kt.append("""    SeedTextilesInvoice(
        company = %s, number = %s, date = %s, type = %s, customerRef = %s,
        lines = listOf(
            %s
        ),
        total = %s, discount = %s, taxAmount = %s, globalDiscountValue = %s, globalDiscountType = %s,
        taxInclusive = %s, showHSN = %s, showDiscountColumn = %s, measureMode = %s,
        terms = %s, notes = %s,
        customer = %s
    )""" % (s(TX_SHORT), s(no), s(iso(inv["invoiceDate"])), s(inv["invoiceType"]), s(tx_cust_ref),
            ",\n            ".join(lines), num(inv["totalAmount"]), num(t["totalDiscount"]), num(inv["totalTaxAmount"]),
            num(t["globalDiscountValue"]), s(gtype), "true" if o["taxInclusive"] else "false",
            "true" if o["showHSN"] else "false", "true" if o["showDiscountColumn"] else "false", s(o.get("measureMode", "")),
            s(dd["customTerms"]), s(dd["internalNote"]), kmap(customer_fields(dd["client"]))))

PAYMODE = {"bank transfer": "bank_transfer", "card": "card", "cash": "cash", "upi": "upi", "cheque": "cheque"}

def receipt_kt(r, companies, inv_company, customers, default_company):
    no = r["receiptNo"]
    m = re.match(r"RCP/([^/]+)/", no)
    company = m.group(1) if m and m.group(1) in companies else None
    if not company:
        for n in [x.strip() for x in r["againstInvoice"].split(",")]:
            if n in inv_company: company = inv_company[n]; break
    company = company or default_company
    cust = None
    for ref, c in customers.items():
        g = customer_fields(c)
        if g.get("name.ta") == r["clientName"] and (not r["clientAddress"] or g.get("city.ta") == r["clientAddress"]):
            cust = ref; break
    addr = r["clientAddress"]
    if cust:
        g = customer_fields(customers[cust]); name = bi(g.get("name.ta"), g.get("name.en")); address = bi(g.get("city.ta"), g.get("city.en"))
    else:
        name = bi(r["clientName"], r.get("clientNameEn")); address = bi(addr, "")
    return "    SeedReceipt(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)" % (
        s(company), s(no), s(r["date"]), s(cust) if cust else "null", name, address, num(r["amount"]),
        s(PAYMODE[r["paymentMode"].lower()]), s(r.get("referenceNo", "")), s(r.get("note", "")), s(r["againstInvoice"]))

tx_rcp_kt = [receipt_kt(r, {TX_SHORT, "SJS"}, {}, tx_customers, TX_SHORT) for r in D["patrugal"].values()]
# "SJS" is not a business here: those receipts belong to the one silk business
tx_rcp_kt = [x.replace('SeedReceipt("SJS"', 'SeedReceipt(%s' % s(TX_SHORT)) for x in tx_rcp_kt]


# ================= FACTORY (coolie) =================
fx_company_short = {k: v["shortBusinessName"] for k, v in D["coolie_thannilai"].items()}
fx_profiles_kt = []
order = sorted(D["coolie_thannilai"].items(), key=lambda kv: -sum(1 for b in D["coolie_pattiyalkal"].values() if b["company_id"] == kv[0]))
for k, c in order:  # the business with more bills first (it is the one that opens)
    sig = "pvs_signature" if c["shortBusinessName"] == "PVS" else "%s_signature" % c["shortBusinessName"].lower()
    ps = {"receiptLanguage": c["receiptLanguage"], "defaultPrintLanguage": c["defaultPrintLanguage"],
          "signatoryNames": {"ta": c["authorizedSignatoryName"], "en": ""}}
    fx_profiles_kt.append(profile_kt(
        c["shortBusinessName"], c["name_Tamil"], c["name_English"], c["phone"], c["email"], "",
        (c["address_Tamil"], c["address_English"]), (c["city_Tamil"], c["city_English"]),
        (c["district_Tamil"], c["district_English"]), ("", ""), ("", ""), c["pincode"],
        (c["bankName_Tamil"], c["bankName_English"]), (c["branch_Tamil"], c["branch_English"]), c["accountNo"], c["ifsc"],
        image("%s_logo" % c["shortBusinessName"].lower(), c["logo"]), "", "small",
        image(sig, c["signature"]), c["authorizedSignatoryName"], "", ("", ""), c["themeColor"], ps))
FX_FIRST = order[0][1]["shortBusinessName"]

fx_items = D["coolie_porulgal"]
fx_item_kt = ["    SeedItem(%s, %s, \"\", 0.0, 0.0, \"quantity\", \"Nos\")" % (
    s(k), bi(first(i.get("name_Tamil"), i.get("name")), first(i.get("name_English"), i.get("nameEn")))) for k, i in fx_items.items()]
fx_item_by_ta = {first(i.get("name_Tamil"), i.get("name")): k for k, i in fx_items.items()}

fx_customers = D["coolie_vanigargal"]
fx_cust_kt = [customer_kt(k, c) for k, c in fx_customers.items()]
def fx_cust_ref(b):
    for ref, c in fx_customers.items():
        g = customer_fields(c)
        if g.get("name.ta") == b["customer_name"] and g.get("city.ta") == b["city"]:
            return ref
    raise SystemExit("no customer for " + b["bill_no"])

fx_inv_kt, fx_inv_company = [], {}
for key, b in D["coolie_pattiyalkal"].items():
    short = fx_company_short[b["company_id"]]
    assert b["bill_no"].startswith(short + "-"), b["bill_no"]
    assert not b["custom_charge_name"] and f(b["custom_charge_rs"]) == 0, b["bill_no"]
    fx_inv_company[b["bill_no"]] = short
    lines = []
    for i in b["items"]:
        ta = first(i.get("name_tamil"), i.get("porul")); en = i.get("name_english", "")
        lines.append("SeedFactoryLine(%s, %s, %s, %s)" % (s(fx_item_by_ta[ta]) if ta in fx_item_by_ta else "null", bi(ta, en), num(i["kg"]), num(i["coolie"] or 0)))
        assert abs(sum(math.floor(f(x["kg"]) * f(x["coolie"])) for x in b["items"]) + f(b["courier_rs"]) + f(b["ahimsa_silk_rs"]) - b["grand_total"]) < 0.5, b["bill_no"]
    snap = {}
    snap["name.ta"], snap["name.en"] = b["customer_name"], b["customer_name_en"]
    snap["city.ta"], snap["city.en"] = b["city"], b["city_en"]
    if b["address"]: snap["address.ta"] = b["address"]
    if b["address_en"]: snap["address.en"] = b["address_en"]
    snap = {k: v for k, v in snap.items() if v}
    bank = {"bankDetails": b["bank_details"], "accountNo": b["account_no"], "ifsc": b["ifsc"],
            "showBank": b["show_bank_details"], "showIfsc": b["show_ifsc"]}
    fx_inv_kt.append("""    SeedFactoryInvoice(
        company = %s, number = %s, date = %s, customerRef = %s,
        lines = listOf(%s),
        total = %s, wastageGrams = %s, postalCharges = %s, ahimsaSilkAmount = %s,
        bankDetails = %s,
        customer = %s
    )""" % (s(short), s(b["bill_no"]), s(iso(b["date"])), s(fx_cust_ref(b)), ", ".join(lines), num(b["grand_total"]),
            num(f(b["setharam_grams"])), num(f(b["courier_rs"])), num(f(b["ahimsa_silk_rs"])), kmap({k: str(v).lower() if isinstance(v, bool) else v for k, v in bank.items()}), kmap(snap)))

fx_rcp_kt = [receipt_kt(r, set(fx_company_short.values()), fx_inv_company, fx_customers, FX_FIRST) for r in D["coolie_patrugal"].values()]


# ================= write MockData.kt =================
HEADER = '''package com.elvan.udukkai.data.mock

// GENERATED by kmp/tools/import_rtdb_export.py from the old Firebase RTDB export - do not edit by hand.
// Both dev seeders (TextilesMockDataSeeder, FactoryMockDataSeeder) wipe the app and load exactly this.
// "seed:<name>" images resolve to composeResources/files/seed/<name>.txt (see ReactSeedAssets).

/** A name in both languages; a language the export left empty is left out. */
private fun bi(ta: String, en: String = ""): Map<String, String> =
    buildMap { if (ta.isNotEmpty()) put("ta", ta); if (en.isNotEmpty()) put("en", en) }

class SeedProfile(
    val shortName: String, val businessName: Map<String, String>,
    val phone1: String, val phone2: String, val email: String, val gstin: String, val pan: String,
    val address: Map<String, String>, val city: Map<String, String>, val district: Map<String, String>,
    val state: Map<String, String>, val country: Map<String, String>, val pinCode: String,
    val bankName: Map<String, String>, val branch: Map<String, String>, val accountNumber: String, val ifsc: String,
    val logo: String, val wideLogo: String, val headerLayout: String, val signature: String, val signatoryName: String,
    val tagline: Map<String, String>, val themeColor: String,
    /** Print-only settings, saved as BusinessProfile.printSettingsJson. */
    val printSettings: Map<String, Any?>
)

/** [ref] is the id the old export used; invoice lines and receipts point at it. */
class SeedItem(
    val ref: String, val name: Map<String, String>, val hsnCode: String, val price: Double, val taxRate: Double,
    val quantityType: String, val unit: String
)

class SeedCustomer(
    val ref: String, val name: Map<String, String>, val address: Map<String, String>, val city: Map<String, String>,
    val district: Map<String, String>, val state: Map<String, String>, val country: Map<String, String>,
    val pinCode: String, val gstin: String, val email: String, val phone: String
)

class SeedLine(
    val itemRef: String?, val name: Map<String, String>, val hsnCode: String, val quantity: Double, val unit: String,
    val price: Double, val taxRate: Double, val discount: Double, val discountType: String
)

class SeedTextilesInvoice(
    val company: String, val number: String, val date: String, val type: String, val customerRef: String,
    val lines: List<SeedLine>, val total: Double, val discount: Double, val taxAmount: Double,
    val globalDiscountValue: Double, val globalDiscountType: String,
    val taxInclusive: Boolean, val showHSN: Boolean, val showDiscountColumn: Boolean, val measureMode: String,
    val terms: String, val notes: String,
    /** The customer as the bill printed it (CustomerSnapshot keys: "name.ta", "city.en", "pinCode" ...). */
    val customer: Map<String, String>
)

class SeedFactoryLine(val itemRef: String?, val name: Map<String, String>, val kg: Double, val rate: Double)

class SeedFactoryInvoice(
    val company: String, val number: String, val date: String, val customerRef: String,
    val lines: List<SeedFactoryLine>, val total: Double,
    val wastageGrams: Double, val postalCharges: Double, val ahimsaSilkAmount: Double,
    /** The bank lines the bill printed (saved as InvoiceModel.bankDetailsJson). */
    val bankDetails: Map<String, String>,
    val customer: Map<String, String>
)

/** [against] is the old "againstInvoice" text: the invoice numbers the receipt paid, in order. */
class SeedReceipt(
    val company: String, val number: String, val date: String, val customerRef: String?,
    val customerName: Map<String, String>, val customerAddress: Map<String, String>, val amount: Double,
    val paymentMode: String, val reference: String, val note: String, val against: String
)

'''

def block(name, typ, rows):
    return "val %s: List<%s> = listOf(\n%s\n)\n\n" % (name, typ, ",\n".join(rows))

out = HEADER
out += block("seedTextilesProfiles", "SeedProfile", [tx_profile])
out += block("seedTextilesItems", "SeedItem", tx_item_kt)
out += block("seedTextilesCustomers", "SeedCustomer", ["    " + customer_kt(tx_cust_ref, tx_cust)])
out += block("seedTextilesInvoices", "SeedTextilesInvoice", tx_inv_kt)
out += block("seedTextilesReceipts", "SeedReceipt", tx_rcp_kt)
out += block("seedFactoryProfiles", "SeedProfile", fx_profiles_kt)
out += block("seedFactoryItems", "SeedItem", fx_item_kt)
out += block("seedFactoryCustomers", "SeedCustomer", ["    " + x for x in fx_cust_kt])
out += block("seedFactoryInvoices", "SeedFactoryInvoice", fx_inv_kt)
out += block("seedFactoryReceipts", "SeedReceipt", fx_rcp_kt)
open(os.path.join(APP, "kotlin", "com", "elvan", "udukkai", "data", "mock", "MockData.kt"), "w", encoding="utf8", newline="\n").write(out)

print("textiles: profiles 1 items %d customers %d invoices %d (skipped duplicates %s) receipts %d" % (
    len(tx_item_kt), 1, len(tx_inv_kt), tx_inv_skipped, len(tx_rcp_kt)))
print("factory:  profiles %d items %d customers %d invoices %d receipts %d" % (
    len(fx_profiles_kt), len(fx_item_kt), len(fx_cust_kt), len(fx_inv_kt), len(fx_rcp_kt)))
print("export:   textiles items %d customers %d invoices %d receipts %d | factory profiles %d items %d customers %d invoices %d receipts %d" % (
    len(D["porulgal"]), len(D["vanigargal"]), len(D["pattiyalkal"]), len(D["patrugal"]),
    len(D["coolie_thannilai"]), len(D["coolie_porulgal"]), len(D["coolie_vanigargal"]), len(D["coolie_pattiyalkal"]), len(D["coolie_patrugal"])))
