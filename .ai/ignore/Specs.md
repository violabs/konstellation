I want you to analyze this project and provide a breakdown of it. 
After, I want you to try to reduce the complexity of it and clean it up
so that it is more maintainable. I was learning as I wrote it so I may
have gotten a bit convoluted. The tests should cover any regression, so you
won't have to worry there.

- Schema unification:
    - Replace multiple schema classes with a sealed model (e.g., Default, Boolean, List, Map, Group, MapGroup) plus one renderer.
    - Keep current classes as thin adapt
    - ers to avoid breaking external code, then deprecate.
- Validation rules centralization:
    - Extract “require not null / not empty” computation to a small analyzer that returns an import set + flags for BuilderGenerator.createFileSpec. Keeps code intent very explicit.
- Generated KDoc:
    - Expand kdoc availability to non-group/list/map-group schemas by sourcing property KDoc from KSP (you already have KDocUtils; can thread it through PropertySchemaService).
- Type alias generation:
    - Simplify generateTypeAliases by building a small descriptor list up front (base, group, map-group) and a single pass to create TypeAliasSpec, avoiding the double map with string checks.
- Tests for resolver:
    - Add a small unit test suite for PropertyKindResolver to lock in classification rules.