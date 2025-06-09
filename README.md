# CocktailBox – Pametna aplikacija za prevzem koktajlov

## Kaj aplikacija omogoča?

- Prijava in registracija z uporabo prepoznave obraza (Face ID)
- Naročilo že pripravljenih koktajlov ali sestava lastnega
- Prevzem naročila iz enega izmed CocktailBox avtomatov po mestu
- Skeniranje QR kode za odpiranje škatle
- Zvočna potrditev in avtomatsko odpiranje ob prisotnosti telefona
- Pošiljanje dnevnika (log) o uspešnosti prevzema

---

## Kako uporabljati aplikacijo?

### 1. Namestitev in zagon
Aplikacijo kot katerokoli drugo aplikacijo odprete na vaši mobilni napravi.

### 2. Registracija in prijava
- Ob prvi uporabi vas aplikacija vodi skozi postopek registracije.
- Fotografirate svoj obraz, sistem pa na podlagi umetne inteligence preveri pristnost (Face ID API).
- Ob uspešni prijavi ste preusmerjeni na začetni zaslon.

### 3. Naročilo koktajla
- Izberite svoj koktajl ali sestavite unikatnega.
- Po oddaji naročila počakajte na obvestilo: "Vaš koktajl je pripravljen."

### 4. Prevzem iz CocktailBoxa
- Obvestilo sproži odprtje QR skenerja.
- Skenirajte QR kodo, ki se nahaja na škatli (paketniku).
- Aplikacija predvaja zvočni signal.
- Ko telefon približate boxu, se ta samodejno odpre.
- Uživajte v svojem napitku!

---

## Kako testiram aplikacijo na svojem računalniku in jo modificiramo

### 1. Prenos projekta iz GitHub repozitorija
Odpri terminal ali ukazno vrstico in vpiši:

```bash
git clone https://github.com/ime-uporabnika/CocktailBox-App.git
```

Po prenosu odpri mapo znotraj Android Studia.

---

### 2. Odpri projekt v Android Studiu
1. Zaženi Android Studio
2. Klikni "Open an existing project"
3. Izberi mapo `CocktailBox-App`
4. Počakaj, da Android Studio izvede Gradle sync
   - V primeru napak preveri, da imaš nameščen Android SDK (API 30 ali novejši)
   - Priporočena je uporaba Java 11

---

### 3. Zagon aplikacije
- Priključi svoj Android telefon (vključen "Developer Mode") ali
- Uporabi vgrajeni Android Emulator
  - Ustvari virtualno napravo z vsaj Android API 30
- Klikni "Run app"

---

### 4. Potrebna dovoljenja
Ob prvem zagonu aplikacija zahteva dovoljenja:

- Kamera (za QR kodo in Face ID)
- Zvok (za predvajanje signala pri odpiranju)
- Internet (komunikacija s strežnikom)

Dovoljenja lahko ročno omogočiš v nastavitvah telefona pod Aplikacije > CocktailBox > Dovoljenja.

---

### 5. Prilagoditev lokalnega IP naslova
Če uporabljaš lasten strežnik za testiranje (npr. Express backend), posodobi IP naslov v datoteki "ApiBox.kt":

```kotlin
.url("http://<TVOJ-IP>:3001/paketniki/<paketnikId>/log")
```

- Telefonska naprava in računalnik morata biti v isti Wi-Fi mreži
- Lokalni IP lahko preveriš z "ipconfig" (Windows) ali "ifconfig" (Mac/Linux)

---

### 6. Odpravljanje težav

- Če Android Studio javi napake pri gradnji:
  - Build > Clean Project
  - Build > Rebuild Project
- Če QR skener ne deluje v emulatorju, uporabi fizično napravo
