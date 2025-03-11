# Intro to Standard WebSocket Implementation

## Code examples

### Excerpt
```java
/**
 * data: DataModel
 */
@Discoverer(packetID = "...", datatype = DataModel.class, protocols = "...")
private final IPacketHandlerBase<DataModel> handler = (data, endpoint, socket, packet, server) -> {
    // [...]
}; 
```

</br>

### Real-Life Examples
Setup used throughout the examples:
```java
// SQL-Editor backend
@UseTon
private ITon ton;
```

**1. Ping**
```java
@Discoverer(
    packetID = "PingPacketData", 
    datatype = PingPacketData.class,
    protocols = Constants.coreProtocolName
)
private final IPacketHandlerBase<PingPacketData> pingHandler = (data, endpoint, socket, packet, server) -> {
    packet.respond(new PongPacketData(
        data.getOutboundTimestamp(), 
        System.currentTimeMillis()
    ), endpoint);
};
```

**2. Checking the availability of a user either by an Email or a usernamme**
```java
@Discoverer(
    packetID = "CheckUserAttributeAvailabilityRequestPacketData", 
    datatype = CheckUserAttributeAvailabilityRequestPacketData.class, 
    protocols =  Constants.coreProtocolName
)
private final IPacketHandlerBase<
    CheckUserAttributeAvailabilityRequestPacketData
> checkUserAttributeAvailabilityHandler = (data, endpoint, socket, packet, server) -> {
    switch (data.getType()) {
        case EMAIL -> { /* [...] */ }
        case USERNAME -> {
            final IUserModule userModule = ton.userModule();
            final UserData ud = userModule.getUserDataOfUsername(data.getAttribute());
            final boolean doesUserExist = ud != null;
            packet.respond(new CheckUserAttributeAvailabilityResponsePacketData(
                doesUserExist
            ), endpoint);
        }
    }
};
```

**3. Creating a new user**
```java
@Discoverer(
    packetID = "CreateUserRequestPacketData", 
    datatype = CreateUserRequestPacketData.class, 
    protocols = Constants.coreProtocolName
)
private final IPacketHandlerBase<CreateUserRequestPacketDat> createUserHandler = (data, endpoint, socket, packet,server) -> {
    final IUserModule userModule = ton.userModule();
    final Date creationData = new Date();
    final UserCreationResult result = userModule.plainCreateAccount(UserData.builder()
            .id(UUID.randomUUID().toString())
            .email(data.getEmail())
            .username(data.getUsername())
            .password(data.getPassword())
            .firstname(data.getFirstname())
            .lastname(data.getLastname())
            .userEntrySetupDate(creationData)
            .lastActive(creationData)
            .build());

    switch (result) {
        case OK -> 
            packet.respond(new CreateUserResponsePacketData(true, result), endpoint);
        case UUID_ALREADY_TAKEN, USERNAME_ALREADY_TAKEN, INTERNAL_ERROR -> 
            packet.respond(new CreateUserResponsePacketData(false, result), endpoint);
    }
};
```

**4. Request a resource from the "CDN"**
```java
@Discoverer(
    packetID = "CDNRequestPacketData", 
    datatype = CDNRequestPacketData.class, 
    protocols = Constants.coreProtocolName
)
private final IPacketHandlerBase<CDNRequestPacketData> cdnRequestHandler = (data, endpoint, socket, packet, server) -> {
    final SocketLaneIdentifyingAttachment sli = Shortcut.useSLI(endpoint);
    final String viewerID = sli != null ? ton.getUserFromSessionID(sli.getSessionID()).getID() : null;
    final CDNRequest request = new CDNRequest(viewerID, data.getBranches());
    final ICDNModule module = ton.cdnModule();
    final CDNResponse response = module.request(request);
    packet.respond(new CDNResponsePacketData(response), endpoint);
};
```
