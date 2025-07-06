![title](https://github.com/user-attachments/assets/f779684c-186a-4db1-b363-3be3e1d56496)

CalamityAPI is a modding API for post processing effects ( shaders ) for minecraft `1.20.1 FORGE`

NOTE: Im not experienced with JAVA or Minecraft code so any fixes and improvements / tips or any sort of help is welcome!<br/>
NOTE2: This mod might be not compatable with optimization mods like Rubidium. Further testing needed!

SETUP:

FILES TO USE
```java
import net.nebula.calamity_api.client.ShaderCore;
import net.nebula.calamity_api.math.CalamityUtils;
```
ShaderCore.REGISTER (1.1.0+)<br/>
main event that handles registraction of shaders<br/>
event contains register function that returns int ID of shader 
```java
int shaderId = event.register(String shader, Function<AdvancedPostPass, Boolean> context, boolean updateOnTick,@Nullable EditType editType, @Nullable Boolean toggleable);
```
see below for more context

HOW TO USE (1.1.0+)
```java
	@SubscribeEvent
	public static void onSetup(ShaderCore.REGISTER event) {
		event.register("calamity_api:simple/color_clamp", effect -> {
			effect.effect().safeGetUniform("Data").set(0.08F,0.91F,0F);
		    return CalamityUtils.inScreenBounds(new Vector3f(0.5F,5.5F,0.5F));
		}, true, ShaderCore.EditType.FIXED, true);
	}
```
`event.REGISTER` accepts (shaderPath, effect function, updateOnTick , EditType , Toggleable )<br/>
`shaderPath` should lead to assets/shader/program/...<br/>
`effect` function should return boolean (if shader should work)<br/>
`updateOnTick` ( fires function on RenderLevelStageEvent event)<br/>
`EditType` controls what part of shader can be edited post render (1.1.0+) `(Nullable)`<br/>
`Toggleabble` controls if your shader can be manually turned off/on (1.1.0+) `(Nullable)`

ShaderCore.POST<br/>
second event that is fired shortly after `ShaderCore.REGISTER`<br/>
ITERATE FUNCTION:
```java
	@SubscribeEvent
	public static void onPost(ShaderCore.POST event) {
		event.iterate(effect -> {
			return null; // iterates trough all registered shaders as Pair (effect.first - ID, effect.second - ShaderPath)
		});
		String shaderPath = event.getShader(shaderId); // returns ShaderPath from ID
		List<Integer> getShaderInts(ShaderType); // returns list of IDs that use specific ShaderPath
	}
```

ShaderCore FUNCTIONS:
(1.1.0+)
```java
boolean success_force = ShaderCore.forceShaderUpdate(ShaderId); // forces a shader to run its function and return result of it

boolean succes_function = ShaderCore.editShader(ShaderId,effect -> { return false; }); // replaces shader function with new one if allowed by EditType
boolean succes_update = ShaderCore.editShader(ShaderId,false); // replaces shader update with new one if allowed by EditType
boolean success_disable = ShaderCore.editShaderToggle(ShaderId,false); // sets working State of shader if allowed by EditType

boolean is_working = ShaderCore.isShaderEnabled(ShaderId); // checks if shader is enabled or disabled (editShaderToggle)
```
Any edit via editShader and editShaderToggle require `ShaderCore.reload()` to work properly

(1.0.0+)
```java
ShaderCore.reload(); // Forced ShaderCore to reload whole shader program
ShaderCore.setForced(true); // Forces shaders to be always ON (if player decided to turn it by pressing F4). Usually handled by gamerule
```

AVAILABLE EditType's (1.1.0+)
```java
ShaderCore.EditType.FIXED // Cannot Edit
ShaderCore.EditType.UPDATEABLE // Can only change updateOnTick
ShaderCore.EditType.FUNCTION // Can only change effect function
ShaderCore.EditType.TOGGLE // Can only change toggleable option for shader
ShaderCore.EditType.ALL // Can Edit Anything

ShaderCore.EditType.UPDT_FUNC // UPDATEABLE + FUNCTION
ShaderCore.EditType.UPDT_TOGL // UPDATEABLE + TOGGLE
ShaderCore.EditType.FUNC_TOGL // FUNCTION + TOGGLE
```
Keep in mind that any other source can edit shader if EditType allows it to

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
returns Vector3f with point on screen data<br/>
X,Y - normalized pixel position on screen [0-1]<br/>
Z - Depth / Distance from screen to worldPos

EFFECT USAGE:
effect function provides `AdvancedPostPass`

```java
effect.effect()
```
returns EffectInstance that can be used to apply uniforms (shader variables)<br/>
Example code for setting position for a "vortex" / "black hole" effect
```java
		event.register("calamity_api:advanced/black_hole", effect -> {
			Vector3f at = new Vector3f(0.5F, 5.5F, 0.5F); // example position
			Vector3f pos = CalamityUtils.worldToScreenPoint(at, 64F); // returns normalized x,y position and raw depth
			if (pos.z <= 0 || !CalamityUtils.inView(at)) return false; // checks if depth is in bounds and on screen (not blocked)
		
			float depth = CalamityUtils.worldSpaceSize(pos.z, 1.0F); // fixed depth
			effect.effect().safeGetUniform("Data").set(pos.x, pos.y, depth); // sets Uniform "Data" to shader
			return true;
		}, true, ShaderCore.EditType.FIXED, true);
```

```java
AdvancedPostPass.sampler(String sampler, ResourceLocation location)
```
applies sampler to shader (loads image to use)<br/>
Example code for shattered screen effect:
```java
		event.register("calamity_api:advanced/cracks", effect -> {
			effect.sampler("HeightmapSampler",new ResourceLocation("calamity_api","textures/noise_heightmap.png"));
			return true;
		}, false, ShaderCore.EditType.FIXED, true);
```

NOTE: Im not experienced with JAVA or Minecraft code so any fixes and improvements / tips or any sort of help is welcome!
