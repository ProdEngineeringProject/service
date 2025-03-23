package ro.unibuc.hello.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.unibuc.hello.data.AuctionEntity;
import ro.unibuc.hello.data.AuctionRepository;
import ro.unibuc.hello.data.BidEntity;
import ro.unibuc.hello.data.BidRepository;
import ro.unibuc.hello.data.ItemEntity;
import ro.unibuc.hello.data.ItemRepository;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.dto.AuctionPlaceBidRequest;
import ro.unibuc.hello.dto.AuctionPost;
import ro.unibuc.hello.dto.AuctionWithAuctioneerAndItem;
import ro.unibuc.hello.dto.BidWithBidder;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.exception.InvalidDataException;

@Component
public class AuctionsService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    public List<AuctionWithAuctioneerAndItem> getAllAuctions() {
        List<AuctionEntity> entities = auctionRepository.findAll();
        return entities.stream()
            .map(AuctionWithAuctioneerAndItem::new)
            .collect(Collectors.toList());
    }

    public AuctionWithAuctioneerAndItem getAuctionById(String id) {
        AuctionEntity entity = auctionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Auction not found"));

        return new AuctionWithAuctioneerAndItem(entity);
    }

    public BidWithBidder getAuctionHighestBid(String id) {
        AuctionEntity entity = auctionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Auction not found"));

        return getAuctionHighestBid(entity)
            .map(BidWithBidder::new)
            .orElse(null);
    }

    public List<BidWithBidder> getAuctionBids(String id) {
        AuctionEntity entity = auctionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Auction not found"));

        return bidRepository.findByAuction(entity).stream()
            .map(BidWithBidder::new)
            .collect(Collectors.toList());
    }

    public AuctionWithAuctioneerAndItem saveAuction(String auctioneerId, AuctionPost auction) {
        AuctionEntity entity = new AuctionEntity();
        entity.setTitle(auction.getTitle());
        entity.setDescription(auction.getDescription());
        entity.setStartPrice(auction.getStartPrice());

        UserEntity user = userRepository.findById(auctioneerId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        entity.setAuctioneer(user);

        ItemEntity item = itemRepository.findById(auction.getItemId())
            .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        entity.setItem(item);

        entity = auctionRepository.save(entity);
        return new AuctionWithAuctioneerAndItem(entity);
    }

    public List<AuctionWithAuctioneerAndItem> saveAll(String auctioneerId, List<AuctionPost> auctions) {
        List<AuctionEntity> entities = auctions.stream()
            .map(auction -> {
                AuctionEntity entity = new AuctionEntity();
                entity.setTitle(auction.getTitle());
                entity.setDescription(auction.getDescription());
                entity.setStartPrice(auction.getStartPrice());

                UserEntity user = userRepository.findById(auctioneerId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
                entity.setAuctioneer(user);

                ItemEntity item = itemRepository.findById(auction.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item not found"));
                entity.setItem(item);

                return entity;
            })
            .collect(Collectors.toList());

        List<AuctionEntity> savedEntities = auctionRepository.saveAll(entities);

        return savedEntities.stream()
            .map(AuctionWithAuctioneerAndItem::new)
            .collect(Collectors.toList());
    }

    public AuctionWithAuctioneerAndItem updateAuction(String id, AuctionWithAuctioneerAndItem auction) {
        AuctionEntity entity = auctionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Auction not found"));

        entity.setTitle(auction.getTitle());
        entity.setDescription(auction.getDescription());
        entity = auctionRepository.save(entity);
        return new AuctionWithAuctioneerAndItem(entity);
    }

    public BidWithBidder placeBid(String id, String userId, AuctionPlaceBidRequest bid) {
        AuctionEntity auction = auctionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Auction not found"));

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check correct bid value
        Optional<BidEntity> highestBid = getAuctionHighestBid(auction);
        if (highestBid.isPresent()) {
            if (highestBid.get().getPrice() <= bid.getPrice()) {
                throw new InvalidDataException("Bid must be higher than highest bid");
            }
        } else {
            if (bid.getPrice() < auction.getStartPrice()) {
                throw new InvalidDataException("Bid can't be lower than starting price");
            }
        }

        // Check that auctioneer can't bid to it's own auction
        if (userId.equals(auction.getAuctioneer().getId())) {
            throw new InvalidDataException("Auctioneer can't bid to it's own auciton");
        }

        BidEntity bidEntity = new BidEntity(bid.getPrice(), user, auction);
        bidEntity = bidRepository.save(bidEntity);
        return new BidWithBidder(bidEntity);
    }
    }

    public void deleteAuction(String id) {
        AuctionEntity entity = auctionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Auction not found"));
        auctionRepository.delete(entity);
    }

    public void deleteAllAuctions() {
        auctionRepository.deleteAll();
    }

    private Optional<BidEntity> getAuctionHighestBid(AuctionEntity auction) {
        return bidRepository.findByAuction(auction).stream()
            .max(Comparator.comparing(BidEntity::getPrice));
    }
}
