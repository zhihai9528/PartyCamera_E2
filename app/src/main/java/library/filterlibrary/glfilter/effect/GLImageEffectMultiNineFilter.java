package library.filterlibrary.glfilter.effect;

import android.content.Context;

import library.filterlibrary.glfilter.utils.OpenGLUtils;

public class GLImageEffectMultiNineFilter extends GLImageEffectFilter {

    public GLImageEffectMultiNineFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/effect/fragment_effect_multi_nine.glsl"));
    }

    public GLImageEffectMultiNineFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
