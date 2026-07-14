# CONTRIBUTING

The development must follow the **Test Driven Development (TDD)** approach.

## Code style

- Do not use blank lines within functions bodies. Empty lines should be used only to separate logical sections between
traits/classes/objects;
- use meaningful names;
- use `camelCase` for functions and variables, `PascalCase` for traits, objects and classes;
- indent with 2 spaces;
- do not overflow lines, break them at 120 characters, respecting the scala style guidelines;
- use named parameters when calling functions only when necessary to improve readability.

## Best practices

- Write the scaladoc for all public APIs;
- write a contract (trait) for each public API, hiding the implementation details;
- always specify the `override` keyword;
- always specify all the types in signatures;
- prefer `val` over `var` and immutable collections over mutable ones;
- `var` variables should be declared as `private` and only accesed/modified through public getter/setter;
- use `Option` instead of `null`;
- use `Either` instead of exceptions for error handling;
- when creating a new ADT, define the `apply` method in the companion object to create a new instance.
- private variables with a getter should be named with a leading underscore, e.g. `_myVar`;
- group related extensions functions;
- import only what is necessary and where it is necessary (not globally);
- should use match case when possible;
- should check input parameters through `require` statements;
- respect DRY (Don't Repeat Yourself) principle;
- exploit modularization, encapsulation and separation of concerns;
- do not follow C-like programming style, embrace OOP and FP paradigms;
- do not use magic numbers or strings, define them as constants.
