package com.static1.fishylottery.model.repositories;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.static1.fishylottery.model.entities.Profile;

import java.util.ArrayList;
import java.util.List;

public class FakeProfileRepository implements IProfileRepository {
    private List<Profile> profiles = new ArrayList<>();
    @Override
    public Task<Void> addProfile(Profile profile) {
        profiles.add(profile);
        return Tasks.forResult(null);
    }

    @Override
    public Task<Void> updateProfile(Profile profile) {
        profiles.removeIf(p -> p.getUid().equals(profile.getUid()));
        profiles.add(profile);
        return Tasks.forResult(null);
    }

    @Override
    public Task<Void> deleteProfile(Profile profile) {
        profiles.removeIf(p -> p.getUid().equals(profile.getUid()));
        return Tasks.forResult(null);
    }

    @Override
    public Task<List<Profile>> getAllProfiles() {
        return Tasks.forResult(profiles);
    }

    @Override
    public Task<Profile> getProfileById(String uid) {
        for (Profile p : profiles) {
            if (p.getUid().equals(uid)) {
                return Tasks.forResult(p); // found
            }
        }
        return Tasks.forResult(null); // not found
    }

    @Override
    public Task<List<Profile>> fetchProfilesByIds(List<String> uids) {
        List<Profile> result = new ArrayList<>();
        for (Profile p : profiles) {
            if (uids.contains(p.getUid())) {
                result.add(p);
            }
        }
        return Tasks.forResult(result);
    }
}
