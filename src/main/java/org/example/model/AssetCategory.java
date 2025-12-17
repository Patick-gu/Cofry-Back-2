package org.example.model;
import javax.persistence.*;
@Entity
@Table(name = "asset_category", schema = "investments")
public class AssetCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;
    public AssetCategory() {
    }
    public AssetCategory(String name) {
        this.name = name;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}