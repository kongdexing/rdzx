package com.example.ysl.mywps.ui.view.autoviewpager;

import android.content.Context;
import android.widget.ImageView;

import com.example.ysl.mywps.R;
import com.squareup.picasso.Picasso;
import com.youth.banner.loader.ImageLoader;

public class GlideImageLoader extends ImageLoader {

    @Override
    public void displayImage(Context context, final Object path, final ImageView imageView) {
        /**
         注意：
         1.图片加载器由自己选择，这里不限制，只是提供几种使用方法
         2.返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
         传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
         切记不要胡乱强转！
         */
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//        imageView.setAdjustViewBounds(true);
//        imageView.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        //Picasso 加载图片简单用法
        Picasso.with(context).load((String) path).placeholder(R.drawable.zhanwei_icon_banner)
                .error(R.drawable.zhanwei_icon_banner)
                .transform(new CropSquareTransformation()).into(imageView);

        //用fresco加载图片简单用法，记得要写下面的createImageView方法
//            Uri uri = Uri.parse((String) path);
//            imageView.setImageURI(uri);

//        imageView.setScaleType(ImageView.ScaleType.CENTER);

//            imageView.setLayoutParams(new LinearLayout.LayoutParams(900, 400));

    }

}