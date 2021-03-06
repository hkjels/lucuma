# 0.5.1

* **[~]** Fixed issue with keyword parsing

# [0.5.0](https://github.com/jeluard/lucuma/issues?q=is%3Aclosed+milestone%3A0.5.0)

`lucuma` now focuses on creating Custom Elements and providing simple ClojureScript hooks.
This release breaks backward compatibility.

* **[+]** Introduced **on-property-changed** callback allowing to be notified of all property/attribute changes
* **[+]** Introduced **mixins** support
* **[+]** Dash-based property name are now supported
* **[~]** Renamed `lucuma` to `lucuma.core` (no more single segment namespace)
* **[~]** Renamed `defwebcomponent` to `defcustomelement`
* **[-]** Removed support for ShadowDOM, **:document** and **:style**
* **[-]** `defcustomelement` cannot take arguments anymore
* **[-]** Old extension mechanism has been removed in favour of new **mixins**

# [0.3.0](https://github.com/jeluard/lucuma/issues?q=is%3Aclosed+milestone%3A0.3.0)

* **[+]** ShadowDOM usage is optional. It can be required at design time via **requires-shadow-dom?**
* **[~]** Replaced **host** with **prototype** and **extends** for more flexibility
* And a bunch of [bugs fixed](https://github.com/jeluard/lucuma/issues?q=label%3Abug+milestone%3A0.3.0+is%3Aclosed).

# [0.2.0](https://github.com/jeluard/lucuma/issues?q=is%3Aclosed+milestone%3A0.2.0)

* **[+]** Introduced function to list defwebcomponent ignored keys
* **[+]** Introduced functions to check for Web Components technologies support
* **[+]** Enhanced **host** syntax to support attributes
* **[+]** Extended **document** and **style** syntax to support media queries
* **[+]** Much improved support for **properties**
* **[~]** Renamed **content** to **document**
* **[~]** Renamed **base-type** to **host**
* **[~]** Renamed **attributes** to **properties**
* **[~]** Renamed hooks (**-fn** to **on-**)
* **[~]** Moved examples to Web Component with real live view support
* **[-]** Removed support for **handler**
* **[-]** Removed support for **constructor**
* **[-]** Removed support for **document** and **style** defined as function
* And a bunch of [bugs fixed](https://github.com/jeluard/lucuma/issues?q=label%3Abug+milestone%3A0.2.0+is%3Aclosed).

# 0.1.0

Initial release with basic WebComponents support.
