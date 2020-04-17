# kitchen

FIXME: Write a one-line description of your library/project.

## Overview

FIXME: Write a paragraph about the library/project and highlight its goals.

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## Example Recipe URLs
```
https://www.allrecipes.com/recipe/165190/spicy-vegan-potato-curry/
https://www.allrecipes.com/recipe/8667/jays-jerk-chicken/
https://www.allrecipes.com/recipe/223042/chicken-parmesan/
https://www.allrecipes.com/recipe/233398/summer-squash-and-sausage-stew/
https://www.allrecipes.com/recipe/25333/vegan-black-bean-soup/
https://www.allrecipes.com/recipe/221293/vegan-black-bean-quesadillas/
https://www.allrecipes.com/recipe/85452/homemade-black-bean-veggie-burgers/
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
