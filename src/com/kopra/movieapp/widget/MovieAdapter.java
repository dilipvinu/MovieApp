package com.kopra.movieapp.widget;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.kopra.movieapp.R;
import com.kopra.movieapp.net.VolleyManager;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MovieAdapter extends ArrayAdapter<JSONObject> {

	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	
	public MovieAdapter(Context context, List<JSONObject> list) {
		super(context, R.layout.list_item_movie, list);
		mInflater = LayoutInflater.from(context);
		mImageLoader = VolleyManager.getInstance(context).getImageLoader();
	}
	
	public static class ViewHolder {
		public NetworkImageView poster;
		public TextView title;
		public TextView cast;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			view = mInflater.inflate(R.layout.list_item_movie, parent, false);
			holder = new ViewHolder();
			holder.poster = (NetworkImageView) view.findViewById(R.id.poster);
			holder.poster.setDefaultImageResId(R.drawable.film_primary);
			holder.poster.setErrorImageResId(R.drawable.film_primary);
			holder.title = (TextView) view.findViewById(R.id.title);
			holder.cast = (TextView) view.findViewById(R.id.cast);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		try {
			JSONObject movie = getItem(position);
			String title = movie.getString("title");
			String year = movie.getString("year");
			String url = movie.getJSONObject("posters").getString("thumbnail");
			holder.poster.setImageUrl(url, mImageLoader);
			holder.title.setText(title + " (" + year + ")");
			JSONArray cast = movie.getJSONArray("abridged_cast");
			List<String> castList = new ArrayList<String>();
			for (int index = 0; index < cast.length(); index++) {
				JSONObject castMember = cast.getJSONObject(index);
				castList.add(castMember.getString("name"));
			}
			String mainCast = TextUtils.join(", ", castList);
			holder.cast.setText(mainCast);
		} catch (JSONException e) {}
		
		return view;
	}
}
