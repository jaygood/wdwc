-- :name save-message! :! :m
-- :doc creates a new message using the name, message, and timestampe keys
INSERT INTO guestbook
(name, message, timestamp)
VALUES (:name, :message, :timestamp)

-- :name get-messages :? :*
-- :doc selects all avail messages
SELECT * from guestbook