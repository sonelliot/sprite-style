This is a demo project for using a 3D character to create an animated
sprite in a HTML5 game. Since I didn't create the sample character I
haven't uploaded him here, only the pre-rendered and packed version.

This is a ClojureScript project so you will need Leiningen installed
to build. To get the sample going you can run:

```lein figwheel```

Or to simply build the project for deployment:

```lein cljsbuild once dev```

These are the interesting parts of the demo:

```./pack.rb``` - Ruby script that takes the rendered frames from MODO,
downsamples them with ImageMagick and then packs them with
TexturePacker.

```./setup.lxo``` - A sample scene with the camera hierarchy I used for
rendering. All of the cameras are named based on their degree rotation
around the unit circle. This is the naming convention that *pack.rb*
and the animation code is expecting.

```./src/sprite_style``` - Contains all of the ClojureScript code for the
HTML5 app. I use the Pixi.JS MovieClip class for rendering. The
```entities.cljs``` and ```core.cljs``` classes contain the guts of
the demo.

Hope this stuff is useful to someone!
