# Roadmap

---
### Motivation
Das Ton-Backend braucht noch immer einiges an Arbeit. Einige zentrale Funktionen sind Stand 29.01.12022 noch immer nicht implementiert.
Das soll sich in der nächsten Woche ändern. Das Projekt soll in der MVP-Version (*Minimum viable product*) nächstes Wochenende existieren.
In der MVP-Version ist ein Vorzeigen des Produkts vollständig möglich, eine Benutzung jedoch noch nicht, da Projekte weder erstellt noch gelöscht werden können.
Außerdem ist die Template-Funktion nicht implementiert und neue Accounts können noch nicht erstellt werden.
Das Erstellen der Accounts stellt ein wichtiges Feature dar, wird also nach dem MVP direkt implementiert, jedoch nicht für die Prüfung.
---
### Ausblick
Wichtige Features sind im MVP nicht enthalten, sollen aber nach Bedarf möglichst zeitnah implementiert werden.
Zu diesen Features gehört beispielsweise das Erstellen neuer Nutzerkonten.
___
### Todos MVP-Version
- [ ] Smaller classes are better ~ Split up the classes into smaller classes max ~100 lines
- [x] Get some sleep... *(Got too much... now I have headache)*
- [ ] Add event system/support for the ton framework
  - [ ] Add a `utils.union`-event-manager to ITonBase
  - [ ] Create a JRP for registering the event handlers to the event system
  - [ ] Add some basic events to the framework
    - [ ] SessionStartedEvent *(A DB session was started, this event contains the caller of that action, or null if the caller isn't a referable entity e.g. the framework itself)*
    - [ ] ClientDisconnectionEvent *(A websocket was closed, notify the DBModule, that it can unregister the relation in the session lookup. The session lookup contains the SocketServerLanes and the DB sessions, they are listening on. This is important for broadcasting db actions to all the listening clients)*
