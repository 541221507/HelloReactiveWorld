package com.packtpub.rxjava_essentials.chapter4;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.packtpub.rxjava_essentials.R;
import com.packtpub.rxjava_essentials.apps.AppInfo;
import com.packtpub.rxjava_essentials.apps.ApplicationAdapter;
import com.packtpub.rxjava_essentials.apps.ApplicationsList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 2017/3/28 18:19.
 * @desc: DistinctExampleFragment
 */

public class DistinctExampleFragment extends Fragment {
    private static final String TAG = DistinctExampleFragment.class.getSimpleName();

    @BindView(R.id.fragment_example_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_example_swipe_container)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private ApplicationAdapter mAdapter;

    private ArrayList<AppInfo> mAddedApps = new ArrayList<>();
    private Unbinder mUnbinder;

    public DistinctExampleFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_example, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mAdapter = new ApplicationAdapter(new ArrayList<>(), R.layout.applications_list_item);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.myPrimaryColor));
        mSwipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        // Progress
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setRefreshing(true);
        mRecyclerView.setVisibility(View.GONE);

        List<AppInfo> apps = ApplicationsList.getInstance().getList();
        loadList(apps);
        debounce();
    }

    private void loadList(List<AppInfo> apps) {
        mRecyclerView.setVisibility(View.VISIBLE);

        Observable<AppInfo> fullOfDuplicates = Observable.from(apps)
                .take(3)
                .repeat(3);

        fullOfDuplicates.distinct()
                .subscribe(new Observer<AppInfo>() {
            @Override
            public void onCompleted() {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getActivity(), "Something went south!", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onNext(AppInfo appInfo) {
                mAddedApps.add(appInfo);
                mAdapter.addApplication(mAddedApps.size() - 1, appInfo);
            }
        });
    }

    /**
     * 过滤掉由Observable发射的速率过快的数据
     */
    private void debounce() {
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            if (subscriber.isUnsubscribed())
                return;
            try {
                // 产生结果的间隔时间分别为100、200、300...900毫秒
                for (int i = 1; i < 10; i++) {
                    subscriber.onNext(4);
                    Thread.sleep(i * 100);
                }
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread())
                .debounce(700, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "completed!");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "Error:" + e.getMessage());
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "Next:" + integer);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
