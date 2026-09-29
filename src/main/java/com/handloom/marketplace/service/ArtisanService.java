package com.handloom.marketplace.service;

import com.handloom.marketplace.exception.ResourceNotFoundException;
import com.handloom.marketplace.model.Artisan;
import com.handloom.marketplace.repository.ArtisanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtisanService {

    private final ArtisanRepository artisanRepository;

    public ArtisanService(ArtisanRepository artisanRepository) {
        this.artisanRepository = artisanRepository;
    }

    public Artisan findById(Long id) {
        return artisanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artisan not found with ID: " + id));
    }

    public Artisan findByUserId(Long userId) {
        return artisanRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Artisan profile not found for user ID: " + userId));
    }

    public List<Artisan> findAll() {
        return artisanRepository.findAll();
    }

    public List<Artisan> findAllActive() {
        return artisanRepository.findAllActive();
    }

    public void update(Artisan artisan) {
        artisanRepository.update(artisan);
    }

    public int countAll() {
        return artisanRepository.countAll();
    }
}
