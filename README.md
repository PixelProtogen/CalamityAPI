![title](https://github.com/user-attachments/assets/f779684c-186a-4db1-b363-3be3e1d56496)
CalamityAPI is a modding API for post processing effects ( shaders ) for minecraft 1.20.1 FORGE

NOTE: Im not experienced with JAVA or Minecraft code so any fixes and improvements / tips or any sort of help is welcome!

SETUP:

FILES TO USE
```java
import net.nebula.calamity_api.client.ShaderCore;
import net.nebula.calamity_api.math.CalamityUtils;
```

HOW TO USE
```java
@SubscribeEvent
public static void onSetup(ShaderCore.REGISTER event) {
		event.register("calamity_api:simple/color_clamp", effect -> {
			effect.effect().safeGetUniform("Data").set(0.08F,0.91F,0F);
		    return CalamityUtils.inScreenBounds(new Vector3f(0.5F,5.5F,0.5F));
		}, true);
}
```
event.register accets (shaderPath, effect function, updateOnTick )
shaderPath should lead to assets/shader/program/...
effect function should return boolean (if shader should work)
updateOnTick ( fires function on RenderLevelStageEvent event)

AVAILABLE CalamityUtils FUNCTIONS:

CalamityUtils.inView(Vector3f worldPos)
returns boolean if point in world can be seen and not obstructed

CalamityUtils.inScreenBounds(Vector3f worldPos)
returns boolean if point is in bounds of screen

CalamityUtils.worldSpaceSize(float depth, float size)
returns corrected float value for constant effect size

CalamityUtils.worldToScreenPoint(Vector3f worldPos, MaxDist)
returns Vector3f with point on screen data (x,y are normalized values of pixel position on screen [0-1] while z is a distance/depth)
