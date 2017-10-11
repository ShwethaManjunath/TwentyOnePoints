import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Weight } from './weight.model';
import { WeightService } from './weight.service';

@Injectable()
export class WeightPopupService {
    private ngbModalRef: NgbModalRef;

    constructor(
        private modalService: NgbModal,
        private router: Router,
        private weightService: WeightService

    ) {
        this.ngbModalRef = null;
    }

    open(component: Component, id?: number | any): Promise<NgbModalRef> {
        return new Promise<NgbModalRef>((resolve, reject) => {
            const isOpen = this.ngbModalRef !== null;
            if (isOpen) {
                resolve(this.ngbModalRef);
            }

            if (id) {
                this.weightService.find(id).subscribe((weight) => {
                    if (weight.timestamp) {
                        weight.timestamp = {
                            year: weight.timestamp.getFullYear(),
                            month: weight.timestamp.getMonth() + 1,
                            day: weight.timestamp.getDate()
                        };
                    }
                    this.ngbModalRef = this.weightModalRef(component, weight);
                    resolve(this.ngbModalRef);
                });
            } else {
                // setTimeout used as a workaround for getting ExpressionChangedAfterItHasBeenCheckedError
                setTimeout(() => {
                    this.ngbModalRef = this.weightModalRef(component, new Weight());
                    resolve(this.ngbModalRef);
                }, 0);
            }
        });
    }

    weightModalRef(component: Component, weight: Weight): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.weight = weight;
        modalRef.result.then((result) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.ngbModalRef = null;
        }, (reason) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.ngbModalRef = null;
        });
        return modalRef;
    }
}
