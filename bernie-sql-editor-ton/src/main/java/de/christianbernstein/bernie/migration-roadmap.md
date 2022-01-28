# Migration roadmap
___
### Motivation
Es ist nicht mehr viel Zeit, bis zur Abgabe, vllt. noch 2 Monate. 
Irgendwann musste ich das Projekt für die Abgabe vorbereiten, 
also aus einem alten Projekt, welches ziemlich voll mit kleineren / anderen Projekten ist entfernen.
Das wollte ich möglichst weit nach Hinten verlegen, da es eigentlich keine essenzielle Sache für die Benutzung des 
Projekts ist. Aber als ich versucht habe eine weitere Sprache zu diesem Projekt hinzuzufügen, sind wichtige Einstellungen des
Maven-Projekts verloren gegangen bzw. kaputtgegangen. 
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
