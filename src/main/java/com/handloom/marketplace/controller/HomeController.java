package com.handloom.marketplace.controller;

import com.handloom.marketplace.model.Artisan;
import com.handloom.marketplace.model.Category;
import com.handloom.marketplace.model.Product;
import com.handloom.marketplace.service.ArtisanService;
import com.handloom.marketplace.service.CategoryService;
import com.handloom.marketplace.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ArtisanService artisanService;

    public HomeController(ProductService productService, CategoryService categoryService, ArtisanService artisanService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.artisanService = artisanService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Product> featuredProducts = productService.findFeatured(8);
        List<Product> newProducts = productService.findNewProducts(8);
        List<Category> categories = categoryService.findAll();
        List<Artisan> featuredArtisans = artisanService.findAllActive();

        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("categories", categories);
        model.addAttribute("artisans", featuredArtisans);

        return "index";
    }

    @GetMapping("/artisans")
    public String artisansList(Model model) {
        List<Artisan> artisans = artisanService.findAllActive();
        model.addAttribute("artisans", artisans);
        return "artisan/public-list";
    }

    @GetMapping("/artisans/{id}")
    public String artisanProfile(@PathVariable("id") Long id, Model model) {
        Artisan artisan = artisanService.findById(id);
        List<Product> products = productService.findByArtisanId(id);

        model.addAttribute("artisan", artisan);
        model.addAttribute("products", products);
        return "artisan/profile";
    }

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }
}
