![title](https://github.com/user-attachments/assets/f779684c-186a-4db1-b363-3be3e1d56496)
CalamityAPI is a modding API for post processing effects ( shaders ) for minecraft 1.20.1 FORGE

NOTE: Im not experienced with JAVA or Minecraft code so any fixes and improvements / tips or any sort of help is welcome!
NOTE2: This mod might be not compatable with optimization mods like Rubidium. Further testing needed!

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
```java
CalamityUtils.inView(Vector3f worldPos)
```
returns boolean if point in world can be seen and not obstructed

```java
CalamityUtils.inScreenBounds(Vector3f worldPos)
```
returns boolean if point is in bounds of screen

```java
CalamityUtils.worldSpaceSize(float depth, float size)
```
returns corrected float value for constant effect size

```java
CalamityUtils.worldToScreenPoint(Vector3f worldPos, MaxDist)
```
returns Vector3f with point on screen data
X,Y - normalized pixel position on screen [0-1]
Z - Depth / Distance from screen to worldPos

EFFECT USAGE:
effect function provides `AdvancedPostPass`

```java
effect.effect()
```
returns EffectInstance that can be used to apply uniforms (shader variables)
Example code for setting position for a "vortex" / "black hole" effect
```java
		event.register("calamity_api:advanced/black_hole", effect -> {
			Vector3f at = new Vector3f(0.5F, 5.5F, 0.5F); // example position
			Vector3f pos = CalamityUtils.worldToScreenPoint(at, 64F); // returns normalized x,y position and raw depth
			if (pos.z <= 0 || !CalamityUtils.inView(at)) return false; // checks if depth is in bounds and on screen (not blocked)
		
			float depth = CalamityUtils.worldSpaceSize(pos.z, 1.0F); // fixed depth
			effect.effect().safeGetUniform("Data").set(pos.x, pos.y, depth); // sets Uniform "Data" to shader
			return true;
		}, true);
```

```java
AdvancedPostPass.sampler(String sampler, ResourceLocation location)
```
applies sampler to shader (loads image to use)
Example code for shattered screen effect:
```java
		event.register("calamity_api:advanced/cracks", effect -> {
			effect.sampler("HeightmapSampler",new ResourceLocation("calamity_api","textures/noise_heightmap.png"));
			return true;
		}, false);
```

NOTE: Im not experienced with JAVA or Minecraft code so any fixes and improvements / tips or any sort of help is welcome!
