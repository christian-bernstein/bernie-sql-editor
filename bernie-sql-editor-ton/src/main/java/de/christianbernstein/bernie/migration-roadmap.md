# Migration roadmap


### Motivation
Es ist nicht mehr viel Zeit bis zur Abgabe, vllt. noch 2 Monate. 
Irgendwann musste ich das Projekt für die Abgabe vorbereiten, 
also aus einem alten Projekt, welches voll mit kleineren Nebenprojekten war entfernen/herauslösen.
Das wollte ich möglichst weit im Zeitplan nach Hinten verlegen, da es eigentlich keine essenzielle Sache für die Benutzung des SQL-Editors ist. Aber als ich versucht habe eine weitere Sprache zu diesem Projekt hinzuzufügen, sind wichtige Einstellungen des
Maven-Projekts verloren gegangen. 
Da das Reparieren nicht unbedingt einfach und schnell geht, transferiere ich das Projekt in diesem Zuge gleich in ein
eigenes, separates Maven-Projekt.
___
### Packages
- [x] `utils`
  - [x] `utils.misc` ? *(is utils.base a full replacement?)*
  - [x] `utils.event` ?
  - [x] `utils.dynamic` ?
  - [x] `utils.reflection`
  - [x] `utils.document` 
  - [x] `utils.discovery`
  - [x] `utils.tailwind`
  - [x] `utils.db`
  - [x] <s>`asm`</s>  *(Replaced by utils.deprecated.asm)*
  - [x] `utils.deprecated.asm` *(Renamed to utils.asm)*
  - [x] `module`
- [x] `gloria`
