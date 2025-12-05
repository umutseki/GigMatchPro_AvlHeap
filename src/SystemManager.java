import java.util.ArrayList;

public class SystemManager {

    //our gold spevialty, hashmaps <3
    MyHashMap<String, Freelancer> freelancers;
    MyHashMap<String, Customer> customers;

    //freelancers will be stored in these avl trees, and these avl trees will be stored in this hashmap
    MyHashMap<String, AvlTree<Freelancer>> serviceTrees;

    MyHashMap<String, int[]> serviceProfiles;

    // i'll use this class to store the changes in service change functions
    private static class ServiceChangeRequest {
        String freelancerId;
        String newService;
        int newPrice;

        ServiceChangeRequest(String fid, String ns, int np) {
            this.freelancerId = fid;
            this.newService = ns;
            this.newPrice = np;
        }
    }

    // 
    ArrayList<ServiceChangeRequest> serviceChangeQueue;

    public SystemManager() {
        freelancers = new MyHashMap<>();
        customers = new MyHashMap<>();
        serviceTrees = new MyHashMap<>();
        serviceProfiles = new MyHashMap<>();
        serviceChangeQueue = new ArrayList<>();
        initializeServices();
    }
    // service requirements are stored with this function
    private void initializeServices() {
        serviceProfiles.put("paint", new int[] { 70, 60, 50, 85, 90 });
        serviceProfiles.put("web_dev", new int[] { 95, 75, 85, 80, 90 });
        serviceProfiles.put("graphic_design", new int[] { 75, 85, 95, 70, 85 });
        serviceProfiles.put("data_entry", new int[] { 50, 50, 30, 95, 95 });
        serviceProfiles.put("tutoring", new int[] { 80, 95, 70, 90, 75 });
        serviceProfiles.put("cleaning", new int[] { 40, 60, 40, 90, 85 });
        serviceProfiles.put("writing", new int[] { 70, 85, 90, 80, 95 });
        serviceProfiles.put("photography", new int[] { 85, 80, 90, 75, 90 });
        serviceProfiles.put("plumbing", new int[] { 85, 65, 60, 90, 85 });
        serviceProfiles.put("electrical", new int[] { 90, 65, 70, 95, 95 });
    }

    // STARTING THE FUNCITONS


    //starting the functions in the main homework
    public String registerCustomer(String id) {
        if (customers.containsKey(id) || freelancers.containsKey(id)) {
            return "Some error occurred in register_customer.";
        }
        Customer newCustomer = new Customer(id);
        customers.put(id, newCustomer);
        return "registered customer " + id;
    }

    //our ilk göz ağrımız
    public String registerFreelancer(String id, String service, int price, int t, int c, int r, int e, int a) {
        if (customers.containsKey(id) || freelancers.containsKey(id)) {
            return "Some error occurred in register_freelancer.";
        }
        if (!serviceProfiles.containsKey(service)) {
            return "Some error occurred in register_freelancer.";
        }
        if (price <= 0 || t < 0 || t > 100 || c < 0 || c > 100 || r < 0 || r > 100 || e < 0 || e > 100 || a < 0
                || a > 100) {
            return "Some error occurred in register_freelancer.";
        }
        int[] reqs = serviceProfiles.get(service);
        Freelancer f = new Freelancer(id, service, price, t, c, r, e, a, reqs);
        freelancers.put(id, f);
        addFreelancerToTree(f);
        return "registered freelancer " + id;
    }

    // our big boy function, everything depends on this
    public String requestJob(String customerId, String serviceType, int k) {
        Customer cust = customers.get(customerId);
        if (cust == null || !serviceProfiles.containsKey(serviceType)) {
            return "Some error occurred in request_job.";
        }

        //find the related tree
        AvlTree<Freelancer> tree = serviceTrees.get(serviceType);

        if (tree == null || tree.getSize() == 0) {
            return "no freelancers available";
        }

        ArrayList<Freelancer> candidates = tree.findTopK(k, cust.blacklist);
        if (candidates.isEmpty()) {
            return "no freelancers available";
        }

        String result = "available freelancers for " + serviceType + " (top " + candidates.size() + "):";

        for (Freelancer f : candidates) {
            result += "\n" + f.freelancerID;
            result += String.format(" - composite: %d, price: %d, rating: %.1f",
                    f.compositeScore, f.price, f.averageRating);
        }

        Freelancer best = candidates.get(0);

        removeFreelancerFromTree(best);
        best.available = false;  
        best.currentCustomerId = cust.id; 

        cust.employmentCount++; 
        cust.activeFreelancers.put(best.freelancerID, true);  

        result += "\nauto-employed best freelancer: " + best.freelancerID + " for customer " + cust.id;

        return result;
    }

    //for type1 cases, manually matching customers and freelancers by their id's
    public String employFreelancer(String custId, String freelId) {
        Customer cust = customers.get(custId);
        Freelancer f = freelancers.get(freelId);

        if (cust == null || f == null) {
            return "Some error occurred in employ.";
        }

        if (cust.isBlacklisted(freelId)) {
            return "Some error occurred in employ.";
        }

        if (!f.available || f.platformBanned) {
            return "Some error occurred in employ.";
        }

        removeFreelancerFromTree(f);

        f.available = false;
        f.currentCustomerId = cust.id; 

        cust.employmentCount++;
        cust.activeFreelancers.put(freelId, true); 

        return custId + " employed " + freelId + " for " + f.serviceType;
    }

    
    public String completeAndRate(String freelId, int rating) {
        Freelancer f = freelancers.get(freelId);

        //checking our requirements
        if (f == null || rating < 0 || rating > 5) {
            return "Some error occurred in complete_and_rate.";
        }

        if (f.available || f.currentCustomerId == null) {
            return "Some error occurred in complete_and_rate.";
        }

        Customer cust = customers.get(f.currentCustomerId);
        if (cust == null) {
            return "Some error occurred in complete_and_rate.";
        }

       
        f.completeJob(rating);

        double subsidyRate = 0.0;
        String tier = cust.getLoyaltyTier();

        if (tier.equals("SILVER")) {
            subsidyRate = 0.05;
        } else if (tier.equals("GOLD")) {
            subsidyRate = 0.10;
        } else if (tier.equals("PLATINUM")) {
            subsidyRate = 0.15;
        }
        long payment = (long) Math.floor(f.price * (1.0 - subsidyRate));

        cust.totalSpent += payment;
        cust.activeFreelancers.remove(freelId);
        f.available = true;
        f.currentCustomerId = null;
        addFreelancerToTree(f);

        return freelId + " completed job for " + cust.id + " with rating " + rating;
    }
    public String cancelByCustomer(String custId, String freelId) {
        Customer cust = customers.get(custId);
        Freelancer f = freelancers.get(freelId);
        if (cust == null || f == null) {
            return "Some error occurred in cancel_by_customer.";
        }
        if (f.available || f.currentCustomerId == null || !f.currentCustomerId.equals(custId)) {
            return "Some error occurred in cancel_by_customer.";
        }
        cust.activeFreelancers.remove(freelId);
        f.customerCancelledJobs++; 
        cust.cancelledCount++;

        f.available = true;
        f.currentCustomerId = null;

        addFreelancerToTree(f);

        return "cancelled by customer: " + custId + " cancelled " + freelId;
    }

    //if a freelancer gives up from the job
    public String cancelByFreelancer(String freelId) {
        Freelancer f = freelancers.get(freelId);
        if (f == null)
            return "Some error occurred in cancel_by_freelancer.";

        if (f.available || f.currentCustomerId == null) {
            return "Some error occurred in cancel_by_freelancer.";
        }

        String custId = f.currentCustomerId;
        Customer cust = customers.get(custId);

        if (cust != null) {
            cust.activeFreelancers.remove(freelId);
        }

        //giving penalties
        int n = f.completedJobs + f.cancelledJobs+1;
        f.averageRating = (f.averageRating * n) / (n + 1);

        f.cancelledJobs++;
        f.cancelledByFreelancerThisMonth++;

        f.applySkillDegrade();

        f.available = true;
        f.currentCustomerId = null;

        String result = "cancelled by freelancer: " + freelId + " cancelled " + custId;

        if (f.cancelledByFreelancerThisMonth >= 5) {
            f.platformBanned = true;
            f.available = false;  
 
            result += "\nplatform banned freelancer: " + freelId;
        } else {
            addFreelancerToTree(f);
        }

        return result;
    }

   //blacklist operations
    public String blacklist(String custId, String freelId) {
        Customer cust = customers.get(custId);
        Freelancer f = freelancers.get(freelId);

        if (cust == null || f == null) {
            return "Some error occurred in blacklist.";
        }

        if (cust.isBlacklisted(freelId)) {
            return "Some error occurred in blacklist.";
        }

        cust.addToBlacklist(freelId);

        return custId + " blacklisted " + freelId;
    }

    // whitelist operations
    public String unblacklist(String custId, String freelId) {
        Customer cust = customers.get(custId);
        Freelancer f = freelancers.get(freelId);

        if (cust == null || f == null) {
            return "Some error occurred in unblacklist.";
        }

        if (!cust.isBlacklisted(freelId)) {
            return "Some error occurred in unblacklist.";
        }

        cust.removeFromBlacklist(freelId);

        return custId + " unblacklisted " + freelId;
    }

    //for type2 

    public String changeService(String freelId, String newService, int newPrice) {
        Freelancer f = freelancers.get(freelId);

        if (f == null) {
            return "Some error occurred in change_service.";
        }

        if (!serviceProfiles.containsKey(newService)) {
            return "Some error occurred in change_service.";
        }

        if (newPrice <= 0) {
            return "Some error occurred in change_service.";
        }
        serviceChangeQueue.add(new ServiceChangeRequest(freelId, newService, newPrice));
        return "service change for " + freelId + " queued from " + f.serviceType + " to " + newService;
    }

    //starting the new month by all means (nnext semester academic comeback will happen)
    public String simulateMonth() {
        for (String fid : freelancers.keySet()) {
            Freelancer f = freelancers.get(fid);
            if (f.platformBanned) continue;

            boolean statusChanged = false;

            if (f.burnout) {
                if (f.completedThisMonth <= 2) {
                    f.burnout = false;
                    statusChanged = true;
                }
            } else {
                if (f.completedThisMonth >= 5) {
                    f.burnout = true;
                    statusChanged = true;
                }
            }
            f.completedThisMonth = 0;
            f.cancelledByFreelancerThisMonth = 0;

            if (statusChanged) {
                removeFreelancerFromTree(f);   
                f.updateCompositeScore();      
                addFreelancerToTree(f);      
            }
        }
        for (ServiceChangeRequest req : serviceChangeQueue) {
            Freelancer f = freelancers.get(req.freelancerId);
            
            if (f == null || f.platformBanned) continue;

            removeFreelancerFromTree(f);
            f.serviceType = req.newService;
            f.price = req.newPrice;
            f.serviceReqs = serviceProfiles.get(req.newService); 
            f.updateCompositeScore();
            addFreelancerToTree(f);
        }
        serviceChangeQueue.clear();
        for (String cid : customers.keySet()) {
            Customer c = customers.get(cid);
            c.updateLoyaltyTier();
        }

        return "month complete";
    }

    // giving info functions
    public String queryFreelancer(String fid) {
        Freelancer f = freelancers.get(fid);
        if (f == null) {
            return "Some error occurred in query_freelancer.";
        }
        return String.format(
                "%s: %s, price: %d, rating: %.1f, completed: %d, cancelled: %d, skills: (%d,%d,%d,%d,%d), available: %s, burnout: %s",
                f.freelancerID,
                f.serviceType,
                f.price,
                f.averageRating,
                f.completedJobs,
                f.cancelledJobs,
                f.T, f.C, f.R, f.E, f.A,
                f.available ? "yes" : "no",
                f.burnout ? "yes" : "no");
    }

    // giving info functions 2
    public String queryCustomer(String cid) {
        Customer c = customers.get(cid);
        if (c == null) {
            return "Some error occurred in query_customer.";
        }

        int blacklistSize = c.blacklist.keySet().size();

        return String.format(
                "%s: total spent: $%d, loyalty tier: %s, blacklisted freelancer count: %d, total employment count: %d",
                c.id,
                c.totalSpent,
                c.getLoyaltyTier(),
                blacklistSize,
                c.employmentCount);
    }

    public String updateSkill(String fid, int t, int c, int r, int e, int a) {
        Freelancer f = freelancers.get(fid);
        if (f == null) {
            return "Some error occurred in update_skill.";
        }
        if (t < 0 || t > 100 || c < 0 || c > 100 || r < 0 || r > 100 || e < 0 || e > 100 || a < 0 || a > 100) {
            return "Some error occurred in update_skill.";
        }
        removeFreelancerFromTree(f);
        f.setSkills(t, c, r, e, a);
        addFreelancerToTree(f);
        return "updated skills of " + fid + " for " + f.serviceType;
    }

    private void addFreelancerToTree(Freelancer f) {
        if (f.platformBanned || !f.available) {
            return;
        }
        AvlTree<Freelancer> tree = serviceTrees.get(f.serviceType);
        if (tree == null) {
            tree = new AvlTree<>();
            serviceTrees.put(f.serviceType, tree);
        }
        tree.insert(f);
    }

    private void removeFreelancerFromTree(Freelancer f) {
        AvlTree<Freelancer> tree = serviceTrees.get(f.serviceType);
        if (tree != null) {
            tree.delete(f);
        }
    }
}