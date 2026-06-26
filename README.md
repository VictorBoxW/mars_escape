# Mars Escape - RPG 130901 - Progetto Metodologie Di Programmazione

Un videogioco di ruolo (RPG) a turni sviluppato in Java utilizzando Swing per l'interfaccia grafica e Gradle come build system.  
Un astronauta è rimasto bloccato su Marte. La fortezza aliena nelle vicinanze custodisce un cristallo energetico indispensabile come carburante per la nave.  
Il giocatore deve esplorare la fortezza piano per piano, affrontare i nemici in un sistema di combattimento a turni, raccogliere oggetti e chiavi, e infine recuperare il Diamante Energetico per fuggire da Marte.  
I progressi del giocatore sono persistenti grazie a un sistema di salvataggio e caricamento basato sulla serializzazione Java.
 
---

## Come eseguire il progetto

### Prerequisiti

- Java 25 (LTS) o superiore
- Gradle (o utilizzo del Gradle Wrapper incluso nel progetto)
### Istruzioni

```
git clone https://github.com/VictorBoxW/mars_escape.git
cd mars_escape
```

### Build del progetto

```
./gradlew build
```

### Esecuzione

```
./gradlew run
```
 
---

## Come si gioca

- **WASD** o **Frecce direzionali** per muovere l'astronauta
- **O** per interagire con le porte
- **Attack / Dodge** durante il combattimento
- **Use Item** per usare oggetti dall'inventario (dentro e fuori dal combattimento)
- Sconfiggi tutti i nemici di ogni piano per ottenere la chiave di accesso
- Trova il Diamante Energetico nell'ultimo piano e usa chiave + diamante per aprire l'uscita finale
---

## Struttura del progetto

```
app/src/main/java/
├── controller/                    — logica di gioco e handler (combattimento, movimento, transizioni)
├── it/unicam/cs/mpgc/rpg130901/   — entry point (App.java)
├── model/                         — entità di gioco (giocatore, nemici, oggetti, piani, stanze)
├── persistence/                   — salvataggio e caricamento dello stato di gioco
└── view/                          — interfaccia grafica Swing (pannelli, finestre, scena)
```
 
---

## Uso di strumenti di AI

Durante lo sviluppo di questo progetto sono stati utilizzati strumenti di Intelligenza Artificiale (LLM) come supporto all'apprendimento, al debugging e al miglioramento della qualità del codice, senza sostituire il lavoro di progettazione e comprensione logica.

L'AI (Claude) è stata utilizzata in particolare per:

- Troubleshooting e debugging.
- Supporto al Refactoring (Clean Code e principi SOLID).
- Miglioramento della struttura e dell'architettura del codice.
- Generazione dei testi (Lore e dialoghi nemici).
- Revisione della naming convention e della documentazione.


Tutto il codice suggerito è stato analizzato criticamente, compreso nel dettaglio,
testato e riadattato manualmente alle esigenze specifiche e ai design pattern del progetto.

Il codice finale è frutto di scelte progettuali personali.
Gli strumenti AI hanno avuto un ruolo di supporto, non di sostituzione del ragionamento.

Per informazioni ulteriori, consultare la **Wiki del repository**.