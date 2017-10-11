import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { BloodPressure } from './blood-pressure.model';
import { BloodPressureService } from './blood-pressure.service';

@Injectable()
export class BloodPressurePopupService {
    private ngbModalRef: NgbModalRef;

    constructor(
        private modalService: NgbModal,
        private router: Router,
        private bloodPressureService: BloodPressureService

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
                this.bloodPressureService.find(id).subscribe((bloodPressure) => {
                    if (bloodPressure.timestamp) {
                        bloodPressure.timestamp = {
                            year: bloodPressure.timestamp.getFullYear(),
                            month: bloodPressure.timestamp.getMonth() + 1,
                            day: bloodPressure.timestamp.getDate()
                        };
                    }
                    this.ngbModalRef = this.bloodPressureModalRef(component, bloodPressure);
                    resolve(this.ngbModalRef);
                });
            } else {
                // setTimeout used as a workaround for getting ExpressionChangedAfterItHasBeenCheckedError
                setTimeout(() => {
                    this.ngbModalRef = this.bloodPressureModalRef(component, new BloodPressure());
                    resolve(this.ngbModalRef);
                }, 0);
            }
        });
    }

    bloodPressureModalRef(component: Component, bloodPressure: BloodPressure): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.bloodPressure = bloodPressure;
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
