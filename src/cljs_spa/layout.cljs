(ns cljs-spa.layout
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn loading-ui []
  [:div.loading])

(defn page-state-ui []
  (case @(rf/subscribe [:?page-state])
    :loading
    [loading-ui]
    :loaded
    (let [children (r/children (r/current-component))]
      (assert (= 1 (count children)))
      (first children))
    :failed
    [:div ":-("]
    nil
    nil))

(defn not-found-ui []
  [:div "Not Found"])

(defn nav-ui []
  [:nav
   [:a {:href "#/"} "Home"]
   [:span " "]
   [:a {:href "#/users"} "Users"]
   [:span " "]
   [:a {:href "#/users/1"} "User #1"]
   [:span " "]
   [:a {:href "#/users/999"} "Invalid user"]
   [:span " "]
   [:a {:href "#/clock"} "Clock"]])

(defn layout-ui []
  [:div
   [nav-ui]
   [:main
    [:article
     (let [children (r/children (r/current-component))]
       (assert (= 1 (count children)))
       (first children))]]])


(rf/reg-event-db :!page-state
  (fn [db [_ page-state]]
    (assoc db :page-state page-state)))

(rf/reg-sub :?page-state
  (fn [db _]
    (get db :page-state)))