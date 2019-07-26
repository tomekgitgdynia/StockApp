package com.tomk.android.stockapp.models.WatchRepository;

import java.util.ArrayList;

public class WatchListRepository {
    private ArrayList<WatchListItem> repository;


    public WatchListRepository()
    {
        repository = new ArrayList<>();
        repository.add(new WatchListItem("INSR" , "Nasdaq","","","","",""));
        repository.add(new WatchListItem("IBM", "IBM","","","","",""));
        repository.add(new WatchListItem("AAPL" ,  "Apple","","","","",""));
    }

    public ArrayList<WatchListItem> getRepository() {
        return repository;
    }

    public void setRepository(ArrayList<WatchListItem> repository) {
        this.repository = repository;
    }
}
