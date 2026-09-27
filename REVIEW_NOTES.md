# Focal Points for project improvement

This document exists to summarize review notes and points of improve that did not necessarily have a
place in code comments. These are intended to be broad conceptual changes that should be made to the
project.

## Genal architecture

### ViewModel
The purpose of a view model is the manage the state of a particular screen. In the way this project
is set up, the InvoiceViewModel is shared by both screens. Generally, you should not share a view model
between multiple screen. They be less focused and harder to test if you do. Instead, focus on making
a view model for each screen.

The way that you are instantiating view models does not work well with Jetpack Compose for your UI.
Compose specifically has its own view model method that you can instantiate in your parameters of
your screens. If you use Compose view models and instantiate them this way, they will be scoped to your
nav entries and will end their lifecycle when that specific nav entry leaves composition. This is better
for performance and separation of concerns.

### Dependency injection
I highly recommend you look into using Hilt for your dependency graph. You can avoid writing a view
model factory and not have to manually build and inject your dependencies.

### Main activity
Assuming you change to a dependency injection library, you can clean up the main activity. See comments
in code.

### Package structure
In terms of package structure for a single module app, each feature should be its own package.
Each package should not be dependent on anything other than things in core or things that package 
owns. For example,

invoicegen
- core
  - data
  - domain (Shared models and repo interfaces)
  - ui (Shared ui elements and core navigation logic)
  - di
- form
  - data (networks/database logic, repository implementations)
  - domain (Feature models, repository interfaces)
  - ui
    - invoice (Screen composable, view models, and state classes go here)
    - nav (This packages nav entry goes here)
  - di (dependency graph would go here or in core depending on how inclusive they are)
- settings (arguably you could keep settings and form together. Just showing for example)
  - data
  - domain
  - ui

I think it would be ideal to avoid having all of your models in a single file. Each one should be a 
different file in your domain package for that feature.
