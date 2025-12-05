public class Customer {

    //personal infos
    String id;
    int totalSpent;
    String loyaltyTier;

    int employmentCount;
    int cancelledCount;
    MyHashMap<String, Boolean> activeFreelancers;
    
    MyHashMap<String, Boolean> blacklist;

    //basic constructor
    public Customer(String id) {
        this.id = id;
        this.totalSpent = 0;
        this.employmentCount = 0;
        this.blacklist = new MyHashMap<>(); 
        this.loyaltyTier="BRONZE";
        this.employmentCount=0;
        this.cancelledCount=0;
        this.activeFreelancers = new MyHashMap<>();
    }

    //blacklist operations
    public boolean addToBlacklist(String freelancerId) {
        if (blacklist.containsKey(freelancerId)){return false;}
        blacklist.put(freelancerId, true);
        return true;
    }
    
    public void removeFromBlacklist(String freelancerId) {
        blacklist.remove(freelancerId);
    }

    public boolean isBlacklisted(String freelancerId) {
        return blacklist.containsKey(freelancerId);
    }

    //for simulate month, updating the tier specialty
    public void updateLoyaltyTier() {
        int effectiveSpending = totalSpent - (cancelledCount * 250);
        if (effectiveSpending < 0) effectiveSpending = 0;

        if (effectiveSpending < 500) {
            this.loyaltyTier = "BRONZE";
        } else if (effectiveSpending < 2000) {
            this.loyaltyTier = "SILVER";
        } else if (effectiveSpending < 5000) {
            this.loyaltyTier = "GOLD";
        } else {
            this.loyaltyTier = "PLATINUM";
        }
    }

    //using loyalty tier when needed
    public String getLoyaltyTier(){
        return this.loyaltyTier;
    }
}