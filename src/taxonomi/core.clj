(ns taxonomi.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:gen-class))


;;; DATA ;;;
; (id) Node identifier.
; (name) node name.
; (parent) who is the parent node.
; (depth) The height of the node. (in the example above `height(root)=0` and `height(a)=1`)
; (department) Managers should have an extra field specifying the name of the department they are managing.
; (language) Developers should have an extra field specifying the name of the programming language they are strongest in.

(def company
  (atom {:org {; Root
               :97ee4672-4d34-45a2-b982-a32107ca6da3
               {:id "97ee4672-4d34-45a2-b982-a32107ca6da3"
                :name "AB"
                :depth 0
                :department "company"}

              ; Departments
               :f5881978-68c4-42b5-85fb-ede4355b5580
               {:id "f5881978-68c4-42b5-85fb-ede4355b5580"
                :name "CD"
                :depth 1
                :department "sales"
                :parent "97ee4672-4d34-45a2-b982-a32107ca6da3"}

               :72ff0d7a-db8f-4aef-bf87-5aa4aba76110
               {:id "72ff0d7a-db8f-4aef-bf87-5aa4aba76110"
                :name "EF"
                :depth 1
                :department "development"
                :parent "97ee4672-4d34-45a2-b982-a32107ca6da3"}

              ; Sales
               :93ec3109-5180-4c30-b4d8-c39dfd24b3b4
               {:id "93ec3109-5180-4c30-b4d8-c39dfd24b3b4"
                :name "GH"
                :depth 2
                :parent "f5881978-68c4-42b5-85fb-ede4355b5580"}

               :02d5a1b1-9acf-440b-942b-c0d6b722d4f5
               {:id "02d5a1b1-9acf-440b-942b-c0d6b722d4f5"
                :name "IJ"
                :depth 2
                :parent "f5881978-68c4-42b5-85fb-ede4355b5580"}

               :f592c37c-5f03-4273-bd2b-d65c6307a6f9
               {:id "f592c37c-5f03-4273-bd2b-d65c6307a6f9"
                :name "KL"
                :depth 2
                :parent "f5881978-68c4-42b5-85fb-ede4355b5580"}

              ; Development
               :aa7daa2d-c46d-4615-82a0-6ffbcbc5b700
               {:id "aa7daa2d-c46d-4615-82a0-6ffbcbc5b700"
                :name "MN"
                :depth 2
                :parent "72ff0d7a-db8f-4aef-bf87-5aa4aba76110"
                :language "PHP"}

               :8119bfb4-6a99-468f-95b5-c88195a13008
               {:id "8119bfb4-6a99-468f-95b5-c88195a13008"
                :name "OP"
                :depth 2
                :parent "72ff0d7a-db8f-4aef-bf87-5aa4aba76110"
                :language "Javascript"}

               :4898507b-dbfa-43d7-b3a6-5160d5329ef7
               {:id "4898507b-dbfa-43d7-b3a6-5160d5329ef7"
                :name "QR"
                :depth 2
                :parent "72ff0d7a-db8f-4aef-bf87-5aa4aba76110"
                :language "Clojure"}}}))


;;; INTERNAL OPERATIONs ;;;

; (add) a new node to the tree.
; (move) Change the parent node of a given node
; (get-children) Get all child nodes of a given node from the tree. (Just 1 layer of children)

; O(1) add 
; O(1) move
; O(n) get-children 

; swap! node into the organization map 
; node {
; :name : str
; :depth number
; (optional) :parent str
; (optional) :department str
; (optional) :language str } 
;
; here it could have handled setting the depth
; parent depth + 1 or 0 if no parent
(defn add-node [node]
  (let [uuid (.toString (java.util.UUID/randomUUID))]
    (swap!
     company
     assoc-in [:org (keyword uuid)]
     (merge node {:id uuid}))))

; swap parent id for specific node
; :id : str (uuid)
; :new-parent-id : str (uuid)
; here it could have handled updating the depth
; parent depth + 1
(defn move-node [id new-parent-id]
  (swap!
   company
   assoc-in [:org (keyword id) :parent]
   new-parent-id))

; Find all with corresponding parent id
; Ex: (get-children "97ee4672-4d34-45a2-b982-a32107ca6da3") => '(x y z)
(defn get-children [id]
  (filter #(= id (:parent (second %))) (:org @company)))


;;; REST INTERFACING ;;;

; Do the json conversion with middleware instead of this nonsense :-D
(defn get-org [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str (:org @company)))})

(defn get-children-endpoint [id]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str (get-children id)))})

(defn move-node-endpoint [id new-parent-id]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str (move-node id new-parent-id)))})

(defn add-node-endpoint [node]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str (add-node node)))})

(defroutes app-routes
  (GET "/org" [] get-org)
  (GET "/org/:id/children" [id] (get-children-endpoint id))
  (POST "/org/:id/move/:new-parent-id" [id new-parent-id] (move-node-endpoint id new-parent-id))
  (POST "/org/add" {body :body} (add-node-endpoint (json/read-str (slurp (io/reader body :encoding "UTF-8")) :key-fn keyword)))
  (route/not-found "Error, page not found!"))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (server/run-server (wrap-defaults #'app-routes api-defaults) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
