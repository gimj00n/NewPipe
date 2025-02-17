package org.schabi.newpipe.info_list.holder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.squareup.picasso.Callback;

import org.schabi.newpipe.R;
import org.schabi.newpipe.database.stream.model.StreamStateEntity;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.info_list.InfoItemBuilder;
import org.schabi.newpipe.ktx.ViewUtils;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.util.DependentPreferenceHelper;
import org.schabi.newpipe.util.Localization;
import org.schabi.newpipe.util.image.PicassoHelper;
import org.schabi.newpipe.util.StreamTypeUtil;
import org.schabi.newpipe.util.text.Translator;
import org.schabi.newpipe.views.AnimatedProgressBar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class StreamMiniInfoItemHolder extends InfoItemHolder {
    public final ImageView itemThumbnailView;
    public final TextView itemVideoTitleView;
    public final TextView itemUploaderView;
    public final TextView itemDurationView;
    private final AnimatedProgressBar itemProgressView;

    StreamMiniInfoItemHolder(final InfoItemBuilder infoItemBuilder, final int layoutId,
                             final ViewGroup parent) {
        super(infoItemBuilder, layoutId, parent);

        itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
        itemVideoTitleView = itemView.findViewById(R.id.itemVideoTitleView);
        itemUploaderView = itemView.findViewById(R.id.itemUploaderView);
        itemDurationView = itemView.findViewById(R.id.itemDurationView);
        itemProgressView = itemView.findViewById(R.id.itemProgressView);
    }

    public StreamMiniInfoItemHolder(final InfoItemBuilder infoItemBuilder, final ViewGroup parent) {
        this(infoItemBuilder, R.layout.list_stream_mini_item, parent);
    }

    @Override
    public void updateFromItem(final InfoItem infoItem,
                               final HistoryRecordManager historyRecordManager) {
        if (!(infoItem instanceof StreamInfoItem)) {
            return;
        }
        final StreamInfoItem item = (StreamInfoItem) infoItem;

//        itemVideoTitleView.setText(item.getName());
//        itemVideoTitleView.setText(Translator.translate(item.getName(), "itemVideoTitle"));
        itemVideoTitleView.setText(item.getName());
//        if (true) {
////            Translator.translate(itemVideoTitleView, "StreamItem");
//            Single.fromCallable(() -> Translator.translateText(itemVideoTitleView, "itemTitle"))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(translatedText -> {
//                        itemVideoTitleView.setText(translatedText);
//                    }, throwable -> {
//                        // Handle the error here
////                        Log.e("UpdateFromItem", "Translation error", throwable);
//                        itemVideoTitleView.setText(item.getName());
//                    });
//        }
//        Single.fromCallable(() -> Translator.translate(item.getName(), "itemTitle"))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(translatedText -> {
//                    itemVideoTitleView.setText(translatedText);
//                }, throwable -> {
//                    // Handle the error here
////                        Log.e("UpdateFromItem", "Translation error", throwable);
//                    itemVideoTitleView.setText(item.getName());
//                });
        Single.fromCallable(() -> Translator.translateText(itemVideoTitleView, "itemTitle"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(translatedText -> {
                    itemVideoTitleView.setText(translatedText);
                }, throwable -> {
                    // Handle the error here
//                        Log.e("UpdateFromItem", "Translation error", throwable);
                    itemVideoTitleView.setText(item.getName());
                });
        itemUploaderView.setText(item.getUploaderName());

        if (item.getDuration() > 0) {
            itemDurationView.setText(Localization.getDurationString(item.getDuration()));
            itemDurationView.setBackgroundColor(ContextCompat.getColor(itemBuilder.getContext(),
                    R.color.duration_background_color));
            itemDurationView.setVisibility(View.VISIBLE);

            StreamStateEntity state2 = null;
            if (DependentPreferenceHelper
                    .getPositionsInListsEnabled(itemProgressView.getContext())) {
                state2 = historyRecordManager.loadStreamState(infoItem)
                        .blockingGet()[0];
            }
            if (state2 != null) {
                itemProgressView.setVisibility(View.VISIBLE);
                itemProgressView.setMax((int) item.getDuration());
                itemProgressView.setProgress((int) TimeUnit.MILLISECONDS
                        .toSeconds(state2.getProgressMillis()));
            } else {
                itemProgressView.setVisibility(View.GONE);
            }
        } else if (StreamTypeUtil.isLiveStream(item.getStreamType())) {
            itemDurationView.setText(R.string.duration_live);
            itemDurationView.setBackgroundColor(ContextCompat.getColor(itemBuilder.getContext(),
                    R.color.live_duration_background_color));
            itemDurationView.setVisibility(View.VISIBLE);
            itemProgressView.setVisibility(View.GONE);
        } else {
            itemDurationView.setVisibility(View.GONE);
            itemProgressView.setVisibility(View.GONE);
        }

        final Callback callback;
        if (false) {
            callback = null;
        } else {
            callback = new Callback() {
                @Override
                public void onSuccess() {
//                    Single.fromCallable(() -> Translator.translate(item.getName(), "itemTitle"))
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(translatedText -> {
//                                itemVideoTitleView.setText(translatedText);
//                            }, throwable -> {
//                                // Handle the error here
////                        Log.e("UpdateFromItem", "Translation error", throwable);
//                                itemVideoTitleView.setText(item.getName());
//                            });
                    try {
                        Translator.translate(itemThumbnailView);
//                        saveImageViewToPNG(itemThumbnailView, itemThumbnailView.getContext(),
//                                "output.png");
                    } catch (final IllegalArgumentException e) {
                        e.printStackTrace();
                        // Handle error (e.g., ImageView does not contain a BitmapDrawable)
                    } catch (final Exception e) {
                        e.printStackTrace();
                        // Handle error
                    }
                    Log.d("Callback", item.getName() + " is back");
                }

                @Override
                public void onError(final Exception e) {

                }
            };
        }
        // Default thumbnail is shown on error, while loading and if the url is empty
        PicassoHelper.loadThumbnail(item.getThumbnails()).into(itemThumbnailView, callback);

        itemView.setOnClickListener(view -> {
            if (itemBuilder.getOnStreamSelectedListener() != null) {
                itemBuilder.getOnStreamSelectedListener().selected(item);
            }
        });

        switch (item.getStreamType()) {
            case AUDIO_STREAM:
            case VIDEO_STREAM:
            case LIVE_STREAM:
            case AUDIO_LIVE_STREAM:
            case POST_LIVE_STREAM:
            case POST_LIVE_AUDIO_STREAM:
                enableLongClick(item);
                break;
            case NONE:
            default:
                disableLongClick();
                break;
        }
    }

    @Override
    public void updateState(final InfoItem infoItem,
                            final HistoryRecordManager historyRecordManager) {
        final StreamInfoItem item = (StreamInfoItem) infoItem;

        StreamStateEntity state = null;
        if (DependentPreferenceHelper.getPositionsInListsEnabled(itemProgressView.getContext())) {
            state = historyRecordManager
                    .loadStreamState(infoItem)
                    .blockingGet()[0];
        }
        if (state != null && item.getDuration() > 0
                && !StreamTypeUtil.isLiveStream(item.getStreamType())) {
            itemProgressView.setMax((int) item.getDuration());
            if (itemProgressView.getVisibility() == View.VISIBLE) {
                itemProgressView.setProgressAnimated((int) TimeUnit.MILLISECONDS
                        .toSeconds(state.getProgressMillis()));
            } else {
                itemProgressView.setProgress((int) TimeUnit.MILLISECONDS
                        .toSeconds(state.getProgressMillis()));
                ViewUtils.animate(itemProgressView, true, 500);
            }
        } else if (itemProgressView.getVisibility() == View.VISIBLE) {
            ViewUtils.animate(itemProgressView, false, 500);
        }
    }

    private void enableLongClick(final StreamInfoItem item) {
        itemView.setLongClickable(true);
        itemView.setOnLongClickListener(view -> {
            if (itemBuilder.getOnStreamSelectedListener() != null) {
                itemBuilder.getOnStreamSelectedListener().held(item);
            }
            return true;
        });
    }

    private void disableLongClick() {
        itemView.setLongClickable(false);
        itemView.setOnLongClickListener(null);
    }

    /**
     * Saves the image from an ImageView to a PNG file.
     *
     * @param imageView the source ImageView
     * @param context   the application context
     * @param filename  the name of the file to save the image to
     * @throws IOException if there's an error during saving
     */
    private static void saveImageViewToPNG(final ImageView imageView,
                                           final Context context,
                                           final String filename)
            throws IOException {
        // Check if the ImageView contains a BitmapDrawable
        if (imageView.getDrawable() instanceof BitmapDrawable) {
            final BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            final Bitmap bitmap = bitmapDrawable.getBitmap();

            // Open a file output stream to save the image
            try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
        } else {
            throw new IllegalArgumentException("ImageView does not contain a BitmapDrawable");
        }
    }
}
