package filterlibrary.glfilter.effect;

import android.content.Context;

import filterlibrary.glfilter.utils.OpenGLUtils;

public class GLImageEffectMultiTwoFilter extends GLImageEffectFilter {

    public GLImageEffectMultiTwoFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/effect/fragment_effect_multi_two.glsl"));
    }

    public GLImageEffectMultiTwoFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
