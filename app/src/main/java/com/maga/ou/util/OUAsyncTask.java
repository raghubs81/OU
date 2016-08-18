package com.maga.ou.util;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;
import com.maga.ou.R;

/**
 * Show a modal dialog with a {@code R.drawable.loading} image while the background task is in progress.
 * The base class should
 * <ul>
 *    <li>Implement {@code doBackgroundTask (Params.. params) method} </li>
 *    <li>Call {@code super.doAfterTaskCompletionBeforeRestoration(Result)} to dismiss the dialog, if it overrides this function.
 * </ul>
 *
 * Created by rbseshad on 21-Jul-16.
 */
public abstract class OUAsyncTask<Params,Result> extends AsyncTask<Params,Void,Result>
{
   private Dialog dialog;

   private Context context;

   public OUAsyncTask(Context context)
   {
      this.context = context;
      ImageView image = new ImageView(context);
      image.setImageResource(R.drawable.loading);
      AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.OU_DialogLoading);
      builder.setView(image, 0, 0, 0, 0);
      dialog = builder.create();
   }

   @Override
   protected void onPreExecute()
   {
      dialog.show();
   }

   @Override
   protected abstract Result doInBackground (Params... params);

   @Override
   protected void onPostExecute (Result result)
   {
      dialog.dismiss();
   }
}
