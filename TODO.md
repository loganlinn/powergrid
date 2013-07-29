*   Remove non-stateful data from game state
*   Separate country-specific logic into various protocols
    *   initial resource market
    *   power plant cards
    *   resource pricing, labels
*   Move player's power-plants out of :players
*   Move :resources & :power-plants under :market

*   Use message types client-side
    *   Move message type defrecords into ^:shared ns
    *   Validate messages w/ instace? instead of expected-topic
    *   Implement a complete? to interface for client-side checks
