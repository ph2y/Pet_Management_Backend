package com.sju18.petmanagement.domain.map.bookmark.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.map.place.dao.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByAuthorAndId(Account author, Long id);
    Optional<Bookmark> findByAuthorAndPlace(Account author, Place place);
    Optional<List<Bookmark>> findAllByAuthorAndFolder(Account author, String folder);
    List<Bookmark> findAllByAuthor(Account author);
    Boolean existsByAuthorAndPlace(Account author, Place place);
    List<Bookmark> findAllByPlace(Place place);
}
