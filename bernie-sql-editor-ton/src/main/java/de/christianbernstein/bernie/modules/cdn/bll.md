```java
@CDN("cat-gif")
ICDNResolver<Gif> catGifResolver = (branch, request, user, api) -> {
  Gif catGif = /* Load a new cat gif */;
  return catGif;
}
```
