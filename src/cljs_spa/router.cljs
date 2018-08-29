(ns cljs-spa.router
  (:require [goog.object :as gobj]
            [reagent.core :as r]
            [doublebundle.router5 :as router5]
            [doublebundle.router5-browser-plugin :as router5-browser-plugin]
            [re-frame.core :as rf]))

(defn handle-load-error [e]
  (when (-> e ex-data :load-error)
    (rf/dispatch [:!page-state :failed]))
  (throw e))

(defn handle-rejection [e]
  (js/console.error e)
  (-> (js/noty. #js{:text (.-message e), :type "error", :timeout 1500})
      .show))

(defn middleware* [name->route to-state from-state]
  (let [on-activate (some-> to-state (gobj/get "name") name->route :on-activate)
        on-deactivate (some-> from-state (gobj/get "name") name->route :on-deactivate)
        p (if on-deactivate
            (-> (js/Promise. (fn [resolve] (resolve (on-deactivate from-state))))
                (.catch handle-rejection))
            (js/Promise.resolve))]
    (-> p
        (.then (fn []
                 (if on-activate
                   (-> (js/Promise. (fn [resolve]
                                      (rf/dispatch [:!page-state :loading])
                                      (resolve {:page-state :loading})))
                       (.then (fn [] (on-activate (js->clj to-state :keywordize-keys true))))
                       (.catch handle-load-error)
                       (.catch handle-rejection))
                   (rf/dispatch [:!page-state :loaded]))
                 true)))))

(defn create-router [routes]
  (let [name->route (->> routes (map (juxt :name identity)) (into {}))]
    (doto (.createRouter router5 (clj->js routes))
      (.usePlugin ((.-default router5-browser-plugin) #js{:useHash true}))
      (.useMiddleware (fn [router]
                        (fn [to-state from-state]
                          (middleware* name->route to-state from-state)))))))

(defn stop-router [router]
  (.stop router))

(defn router-ui [initial-props]
  (let [!unsubscribe-fn (atom nil)
        unsubscribe (fn []
                      (when-let [fun @!unsubscribe-fn]
                        (fun)
                        (reset! !unsubscribe-fn nil)))
        !route (r/atom (.getState (:router initial-props)))
        on-change (fn [o] (reset! !route (gobj/get o "route")))
        subscribe (fn [router]
                    (unsubscribe)
                    (reset! !unsubscribe-fn (.subscribe router on-change)))]
    (let [router (:router initial-props)]
      (reset! !route (.getState router))
      (subscribe router))
    (r/create-class {:component-will-receive-props (fn [_ new-argv]
                                                     (subscribe (-> new-argv second :router)))
                     :component-will-unmount unsubscribe
                     :reagent-render
                     (fn [{:keys [render-fn]}]
                       (assert (fn? render-fn) "Must be a function: render-fn")
                       (let [route @!route]
                         (render-fn {:name (gobj/get route "name")
                                     :params (js->clj (gobj/get route "params") :keywordize-keys true)})))})))
